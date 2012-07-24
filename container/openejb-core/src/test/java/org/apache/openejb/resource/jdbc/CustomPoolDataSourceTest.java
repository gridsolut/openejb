package org.apache.openejb.resource.jdbc;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.resource.jdbc.managed.local.ManagedConnection;
import org.apache.openejb.resource.jdbc.pool.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class CustomPoolDataSourceTest {
    @Resource
    private DataSource ds;


    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("managed", "new://Resource?type=DataSource");
        p.put("managed.DataSourceCreator", CustomCreator.class.getName());
        p.put("managed.JtaManaged", "false");
        p.put("managed.Name", "custom");
        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar();
    }

    @Test
    public void checkCustomCreatorIsUsed() throws SQLException {
        assertNotNull(ds);
        assertTrue(Proxy.isProxyClass(ds.getClass()));
        assertTrue(ds instanceof CustomDataSource);
        assertEquals("custom", ((CustomDataSource) ds).name());
    }

    public static class CustomCreator extends PoolDataSourceCreator {
        @Override
        protected boolean hasReallyCreated(final Object object) {
            return false; // no need
        }

        @Override
        protected void doDestroy(final DataSource dataSource) throws Throwable {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSource pool(String name, DataSource ds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataSource pool(final String name, final String driver, final Properties properties) {
            return (CustomDataSource) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[] { CustomDataSource.class },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("name")) {
                                return properties.getProperty("Name");
                            }
                            return null;
                        }
                    });
        }
    }

    public static interface CustomDataSource extends DataSource {
        String name();
    }
}