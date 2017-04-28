/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\G1\\Network\\sharelibrary\\src\\main\\aidl\\cn\\teemo\\www\\aidl\\ISocketService.aidl
 */
package cn.teemo.www.aidl;
public interface ISocketService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.teemo.www.aidl.ISocketService
{
private static final java.lang.String DESCRIPTOR = "cn.teemo.www.aidl.ISocketService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.teemo.www.aidl.ISocketService interface,
 * generating a proxy if needed.
 */
public static cn.teemo.www.aidl.ISocketService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.teemo.www.aidl.ISocketService))) {
return ((cn.teemo.www.aidl.ISocketService)iin);
}
return new cn.teemo.www.aidl.ISocketService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_send:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
byte[] _arg2;
_arg2 = data.createByteArray();
this.send(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.teemo.www.aidl.ISocketService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// 协议类型，json数据，二进制数据转出string
// // 因为AIDL传参数的时候bin为数组，不能为null,如果数据不带bin，传new byte[0]

@Override public void send(int type, java.lang.String json, byte[] bin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
_data.writeString(json);
_data.writeByteArray(bin);
mRemote.transact(Stub.TRANSACTION_send, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_send = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
// 协议类型，json数据，二进制数据转出string
// // 因为AIDL传参数的时候bin为数组，不能为null,如果数据不带bin，传new byte[0]

public void send(int type, java.lang.String json, byte[] bin) throws android.os.RemoteException;
}
