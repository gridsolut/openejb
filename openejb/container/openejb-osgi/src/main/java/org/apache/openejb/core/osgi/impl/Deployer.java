/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.osgi.impl;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.RpcContainer;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.loader.SystemInstance;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class Deployer implements BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                deploy(event.getBundle());
                break;
            case BundleEvent.STOPPED:
                undeploy(event.getBundle());
                break;
        }
    }

    private void deploy(Bundle bundle) {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(new OSGIClassLoader(bundle));

        LOGGER.info(String.format("[Deployer] Bundle %s has been started", bundle.getSymbolicName()));

        LOGGER.info(String.format("[Deployer] Checking whether it's an EJB module"));
        try {
            Enumeration<URL> e = bundle.findEntries("META-INF", "ejb-jar.xml", false);
            if (e != null && e.hasMoreElements()) {
                URL ejbJarUrl = e.nextElement();

                LOGGER.info("[Deployer] It's an EJB module: " + ejbJarUrl);

                LOGGER.info("[Deployer] Deploying onto OpenEJB");

                String location = bundle.getLocation();
                LOGGER.info("[Deployer] bundle location: " + location);
                try {
                    File file = new File(new URL(location).getFile());
                    try {
                        DeploymentLoader deploymentLoader = new DeploymentLoader();
                        AppModule appModule = deploymentLoader.load(file);

                        ConfigurationFactory configurationFactory = new ConfigurationFactory();
                        AppInfo appInfo = configurationFactory.configureApplication(appModule);

                        Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                        LOGGER.debug("Assembler : " + assembler);
                        LOGGER.debug("AppInfo id: " + appInfo.appId);
                        AppContext appContext = assembler.createApplication(appInfo);

                        LOGGER.info("[Deployer] Application deployed: " + appInfo.path);

                        registerService(bundle, appContext);

                    } catch (Exception ex) {
                        LOGGER.error("can't deploy " + ejbJarUrl.toExternalForm(), ex);
                    }
                } catch (Exception ex1) {
                    LOGGER.error("can't deploy " + ejbJarUrl.toExternalForm(), ex1);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private void undeploy(Bundle bundle) {
        LOGGER.info(String.format("[Deployer] Bundle %s has been stopped", bundle.getSymbolicName()));

        // Let's others finish what needs to be here
        // It should leave openejb in the state as if the ejb had not been deployed at all

        // Step 1. Check whether it's an ejb (cf. deploy method above)

        // Step 2. Unregister a service (cf. deploy method above)
    }

    /**
     * Register OSGi Service for EJB so calling the service will actually call the EJB
     *
     * @param bundle
     * @param appContext
     */
    private void registerService(Bundle bundle, AppContext appContext) {
        LOGGER.info("[Deployer] Registering a service for the EJB");
        BundleContext context = bundle.getBundleContext();
        for (BeanContext beanContext : appContext.getBeanContexts()) {
            try {
                if (beanContext.getBusinessLocalInterface() != null) {
                    registerService(beanContext, context, beanContext.getBusinessLocalInterfaces());
                }
                if (beanContext.getBusinessRemoteInterface() != null) {
                    registerService(beanContext, context, beanContext.getBusinessRemoteInterfaces());
                }
                if (beanContext.isLocalbean()) {
                    registerService(beanContext, context, Arrays.asList(beanContext.getBusinessLocalBeanInterface()));
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[Deployer] can't register: %s", beanContext.getEjbName()));
            }
        }
    }

    private void registerService(BeanContext beanContext, BundleContext context, List<Class> interfaces) {
        if (!interfaces.isEmpty()) {
            Class<?>[] itfs = interfaces.toArray(new Class<?>[interfaces.size()]);
            try {
                Object service = Proxy.newProxyInstance(itfs[0].getClassLoader(), itfs, new Handler(beanContext));
                context.registerService(str(itfs), service, new Properties());
                LOGGER.info(String.format("[Deployer] EJB registered: %s for interfaces %s", beanContext.getEjbName(), interfaces));
            } catch (IllegalArgumentException iae) {
                LOGGER.error(String.format("[Deployer] can't register: %s for interfaces %s", beanContext.getEjbName(), interfaces));
            }
        }
    }

    private String[] str(Class<?>[] itfs) {
        String[] itfsStr = new String[itfs.length];
        for (int i = 0; i < itfs.length; i++) {
            itfsStr[i] = itfs[i].getName();
        }
        return itfsStr;
    }

    private static class Handler implements InvocationHandler {
        private BeanContext beanContext;

        public Handler(BeanContext bc) {
            beanContext = bc;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final RpcContainer container = RpcContainer.class.cast(beanContext.getContainer());
            return container.invoke(beanContext.getDeploymentID(),
                    beanContext.getInterfaceType(method.getDeclaringClass()),
                    method.getDeclaringClass(), method, args, null);
        }
    }

    private static class OSGIClassLoader extends ClassLoader {
        private final Bundle backingBundle;

        public OSGIClassLoader(Bundle bundle) {
            super(null);
            backingBundle = bundle;
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            try {
                return this.backingBundle.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]", cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle [" + backingBundle + "]");
                e.initCause(ncdfe);
                throw e;
            }
        }

        protected URL findResource(String name) {
            return backingBundle.getResource(name);
        }

        protected Enumeration findResources(String name) throws IOException {
            return backingBundle.getResources(name);
        }

        public URL getResource(String name) {
            return findResource(name);
        }

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        public String toString() {
            return "OSGIClassLoader for [" + backingBundle + "]";
        }
    }
}