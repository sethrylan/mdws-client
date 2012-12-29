
/**
 * A Java client for 
 * <ul>
 * <li>Medical Record Data (EMR) Service</li>
 * <li>MyHealtheVet (MHV) Service</li>
 * <li>Athena (ATH) Service</li>
 * </ul>
 * 
 * Login procedure is getVha -> getVisn(id) -> connect(siteCode) -> login(accessCode, verifyCode, contextType)
 * 
 * To install MDWS services on the preconfigured <a href="http://www.osehra.org/wiki/virtual-machine-testing-osehra-code-base-pre-configured-2">OSEHREA+MUnit VM</a>
 * 
 * # Install prerequistes:
 * sudo apt-get update && apt-get install mono-devel mono-xsp asp.net-examples monodevelop nunit monodevelop-nunit python-dev
 * cd ~/src
 * 
 * # Download Source
 * git clone https://github.com/ChristopherEdwards/MDWS.git
 * cd MDWS
 * git checkout MonoSupport
 * 
 * # Add SQL libraries
 * cd ~/Downloads
 * wget http://downloads.sourceforge.net/project/sqlite-dotnet2/SQLite%20for%20ADO.NET%202.0/1.0.66.0/SQLite-1.0.66.0-managedonly-binaries.zip?r=&ts=1354650067&use_mirror=hivelocity -o SQLite-1.0.66.0-managedonly-binaries.zip
 * wget http://www.sqlite.org/sqlite-dll-win32-x86-3071300.zip
 * unzip -j SQLite-1.0.66.0-managedonly-binaries.zip \*System.Data.SQLite.*
 * unzip -j sqlite-dll-win32-x86-3071300.zip \*sqlite3.d*
 * mkdir ~/src/MDWS/mdo/mdo/resources/lib/sqlite
 * cp sqlite3.def sqlite3.dll System.Data.SQLite.XML System.Data.SQLite.dll /home/softhat/src/MDWS/mdo/mdo/resources/lib/sqlite
 * 
 * # Add Log4net
 * wget http://archive.apache.org/dist/incubator/log4net/1.2.10/incubating-log4net-1.2.10.zip
 * unzip -j incubating-log4net-1.2.10.zip \*log4net.*
 * cp log4net.dll log4net.pdb log4net.xml ~/src/MDWS/mdo/mdo-test/resources/lib/log4net
 * 
 * TODO: Nunit, Spring.Net, TOReflection, Compile MDWS, Configure MDWS,Starting MDWS, Testing MDWS, 
 * @see https://sandbox.vainnovation.us/groups/mdws/
 */

package gov.va.iehr.mdws;

