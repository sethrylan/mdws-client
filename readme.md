# MDWS Client

A thread-safe java client for [MDWS](https://sandbox.vainnovation.us/groups/mdws/) SOAP services.

See also: [connection information](https://sandbox.vainnovation.us/groups/mdws/wiki/b800c/Connecting_to_the_Sandbox_MDWS_Server.html) and [SOAP message list](https://sandbox.vainnovation.us/groups/mdws/wiki/085db/Drafting__MDWS_SOAP_Message_Reference_List.html) for MDWS.

## Test Service Logins

<table>
  <tr>
    <th>login</th>
    <th>password</th>
    <th>Works for site 901 (CPM)</th>
    <th>Works for site 902 (UVA)</th>
  </tr>
  <tr>
    <th>1programmer</th>
    <td>programmer1</td>
    <td>Yes</td>
    <td>No</td>
  </tr>
  <tr>
    <th>01provider</th>
    <td>provider01</td>
    <td>No</td>
    <td>No</td>
  </tr>
  <tr>
    <th>01radiologist</th>
    <td>radiologist01</td>
    <td>No</td>
    <td>No</td>
  </tr>
  <tr>
    <th>01vehu</th>
    <td>vehu01</td>
    <td>Yes</td>
    <td>No</td>
  </tr>
  <tr>
    <th>04vehu</th>
    <td>vehu04</td>
    <td>Yes</td>
    <td>No</td>
  </tr>
  <tr>
    <th>1pharmasist</th>
    <td>pharmasist1</td>
    <td>No</td>
    <td>No</td>
  </tr>
</table>



## Example MDWS use cases

### Double login produces error
getVHA -> connect(901) -> login(1programmer) -> getWards [ok] -> login(1programmer)  -> getWards

Fault message after second login:
```
The remote procedure XUS AV CODE is not registered to the option XUS SIGNON.
```
Fault message on method invocation after second login:
```
Application context has not been created!
```

### Multi-site queries
connect->login->setupMultiSiteQuery->getAllMeds()  
	or  
visit->setupMultiSiteQuery->getAllMeds()  

Note: not all calls are multisite; select() is not multisite

## Build Instructions

Build in maven
```
mvn clean verify
```

## Open Development Items
* catch fault messages for all invocations
* multisitequery
* retrieve broken XML in getRpcs invocation.

## Questions about MDWS
1. Does connect(sitecode).getItems() ever contain more than one DataSourceTO?
1. Why are there separate domain objects for each service? I.e., a gov.va.medora.mdws.emrsvc/mhvservice/schedulingsrv.DataSourceTO and a *PatientService, FacilityService, ClinicService, and other SubDomain services.
	* But then you ended up with separate domain objects on the client side because of the separate namespaces
1. What does "The remote procedure XUS AV CODE is not registered to the option XUS SIGNON." mean?
	* To fix: register the remote procedure XUS SIGNON SETUP with the option OR CPRS GUI CHART
1. Are there any methods other than getVHA that start a session?
	* pretty much anything
1. Connection timeout?
	* "kinda low"; you have both a ASP.NET session (20 minutes) and a MDWS->VistA session (5 minutes?)
1. Second login produces fault : "The remote procedure XUS AV CODE is not registered to the option XUS SIGNON."
1. Why does login() only log onto the first site in the list of currently connected sites?
	* just cause; use setupMultisiteQuery() instead of multiple connects.
1. What methods don't require a connection?
1. Most of the RPCS are in the CPRS context (menu option), but some (DDR RPC, for example) are generic calls
1. Operations that need selection:
    - getAllOrders?, getAllergies?, getAllMeds?



## License

[sethrylan.mit-license.org](http://sethrylan.mit-license.org/)
