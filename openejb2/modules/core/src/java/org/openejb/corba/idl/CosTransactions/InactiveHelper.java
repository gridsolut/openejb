package org.openejb.corba.idl.CosTransactions;


/**
* org/apache/geronimo/interop/CosTransactions/InactiveHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.1"
* from C:/dev/corba/geronimo/trunk/modules/interop/src/idl/CosTransactions.idl
* Saturday, March 12, 2005 1:30:01 PM EST
*/

abstract public class InactiveHelper
{
  private static String  _id = "IDL:CosTransactions/Inactive:1.0";

  public static void insert (org.omg.CORBA.Any a, org.openejb.corba.idl.CosTransactions.Inactive that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.openejb.corba.idl.CosTransactions.Inactive extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [0];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          __typeCode = org.omg.CORBA.ORB.init ().create_exception_tc (org.openejb.corba.idl.CosTransactions.InactiveHelper.id (), "Inactive", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.openejb.corba.idl.CosTransactions.Inactive read (org.omg.CORBA.portable.InputStream istream)
  {
    org.openejb.corba.idl.CosTransactions.Inactive value = new org.openejb.corba.idl.CosTransactions.Inactive ();
    // read and discard the repository ID
    istream.read_string ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.openejb.corba.idl.CosTransactions.Inactive value)
  {
    // write the repository ID
    ostream.write_string (id ());
  }

}
