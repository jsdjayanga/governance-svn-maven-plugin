package governance.plugin.webapps;

/**
 * Created by jayanga on 2/11/14.
 */
public class WebAppGovernanceSOAPMessageCreator {
    public static String  createAddWebAppRequest(String name, String namespace, String serviceclass, String displayname, String version, String description){
        StringBuffer soapRequest = new StringBuffer();
        soapRequest.append("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' ");
        soapRequest.append("xmlns:ser='http://services.add.webapplications.governance.carbon.wso2.org'>");
        soapRequest.append("<soapenv:Header/>");
        soapRequest.append("<soapenv:Body>");
        soapRequest.append("<ser:addWebApplication>");
        soapRequest.append("<ser:info><![CDATA[<metadata xmlns='http://www.wso2.org/governance/metadata'><overview><name>");
        soapRequest.append(name);
        soapRequest.append("</name>");
        soapRequest.append("<namespace>");
        soapRequest.append(namespace);
        soapRequest.append("</namespace>");
        soapRequest.append("<serviceclass>");
        soapRequest.append(serviceclass);
        soapRequest.append("</serviceclass>");
        soapRequest.append("<displayname>");
        soapRequest.append(displayname);
        soapRequest.append("</displayname>");
        soapRequest.append("<version>");
        soapRequest.append(version);
        soapRequest.append("</version>");
        soapRequest.append("<description>");
        soapRequest.append(description);
        soapRequest.append("</description>");
        soapRequest.append("</overview></metadata>]]></ser:info>");
        soapRequest.append("</ser:addWebApplication>");
        soapRequest.append("</soapenv:Body>");
        soapRequest.append("</soapenv:Envelope>");
        return soapRequest.toString();
    }
}
