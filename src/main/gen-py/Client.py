import sys
sys.path.append('./gen-py')

from com.csci.thrift.democaptha import DemoCaptcha
from com.csci.thrift.democaptha.ttypes import *;

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TCompactProtocol

from PIL import Image

try:

    # Make socket
    transport = TSocket.TSocket('127.0.0.1', 9988)

    # Buffering is critical. Raw sockets are very slow
    transport = TTransport.TBufferedTransport(transport)

    # Wrap in a protocol
    protocol = TCompactProtocol.TCompactProtocol(transport)

    # Create a client to use the protocol encoder
    client = DemoCaptcha.Client(protocol)


    # Connect!
    transport.open()

    picPath = raw_input("Please input file path> ")
    if picPath.startswith("http"):
        import urllib2
        response = urllib2.urlopen(picPath.strip())
        f = response.read()
        b = bytearray(f)
        print b
        result = client.getDllCaptchaFromImageBinary(b)
        print 'Captcha Result ' + result
    else:
        with open(picPath.strip(), "rb") as imageFile:
            try:
                f = imageFile.read()
                b = bytearray(f)
                result = client.getDllCaptchaFromImageBinary(b)
                print 'Captcha Result ' + result
            finally:
                imageFile.close()
    # Close!
    transport.close()

except Thrift.TException, tx:
    print '%s' % (tx.message)