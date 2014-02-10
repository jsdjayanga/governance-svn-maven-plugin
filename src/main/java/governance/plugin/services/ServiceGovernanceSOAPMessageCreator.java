package governance.plugin.services;

/**
 * Created by jayanga on 2/9/14.
 */
public class ServiceGovernanceSOAPMessageCreator {
    public static String  createAddServiceRequest(String name, String namespace, String version, String type, String description){
        StringBuffer soapRequest = new StringBuffer();
        soapRequest.append("<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' ");
        soapRequest.append("xmlns:ser='http://services.add.service.governance.carbon.wso2.org'>");
        soapRequest.append("<soapenv:Header/>");
        soapRequest.append("<soapenv:Body>");
        soapRequest.append("<ser:addService>");
        soapRequest.append("<ser:info><![CDATA[<metadata xmlns='http://www.wso2.org/governance/metadata'><overview><name>");
        soapRequest.append(name);
        soapRequest.append("</name>");
        soapRequest.append("<namespace>");
        soapRequest.append(namespace);
        soapRequest.append("</namespace>");
        soapRequest.append("<version>");
        soapRequest.append(version);
        soapRequest.append("</version>");
        soapRequest.append("<types>");
        soapRequest.append(type);
        soapRequest.append("</types>");
        soapRequest.append("<description>");
        soapRequest.append(description);
        soapRequest.append("</description>");
        soapRequest.append("</overview></metadata>]]></ser:info>");
        soapRequest.append("</ser:addService>");
        soapRequest.append("</soapenv:Body>");
        soapRequest.append("</soapenv:Envelope>");
        return soapRequest.toString();
    }
}
