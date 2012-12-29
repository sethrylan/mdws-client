package gov.va.iehr.mdws;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gaineys
 */
public class RawXMLHandler {

    
    final static org.slf4j.Logger logger = LoggerFactory.getLogger(RawXMLHandler.class);

    public static class LogicalHandler implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

        @Override
        public void close(MessageContext mc) {
        }

        @Override
        public boolean handleFault(LogicalMessageContext messagecontext) {
            return true;
        }

        @Override
        public boolean handleMessage(LogicalMessageContext mc) {
            Boolean outboundProperty = (Boolean)mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (outboundProperty.booleanValue()) {
                System.out.print("Outbound message:");
            } else {
                System.out.print("Inbound message:");
            }
            
            System.out.println(mc.get(MessageContext.WSDL_OPERATION).toString());
            
            if (mc.get(MessageContext.WSDL_OPERATION).toString().contains("getRpcs")) {
                Source source = null;
                try {
                    source = mc.getMessage().getPayload();
                } catch (RuntimeException ex) {
                    ex.printStackTrace();
                    return false;
                }
                TransformerFactory tFactory = TransformerFactory.newInstance();
                try {
                    Transformer transformer = tFactory.newTransformer();
                    StreamResult result = new StreamResult(System.out);
                    transformer.transform(source, result); 
                } catch (TransformerConfigurationException ex) {
                    logger.error(null, ex);
                } catch (TransformerException ex) {
                    logger.error(null, ex);
                }
                
            }
                        
            return true;
        }
    }

    public static class SOAPHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

        @Override
        public Set<QName> getHeaders() {
            return null;
        }

        @Override
        public void close(MessageContext mc) {
        }

        @Override
        public boolean handleFault(SOAPMessageContext mc) {
            return true;
        }

        @Override
        public boolean handleMessage(SOAPMessageContext mc) {
            
            Boolean outboundProperty = (Boolean) mc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (outboundProperty.booleanValue()) {
                System.out.print("Outbound message:");
            } else {
                System.out.print("Inbound message:");
            }
            
            System.out.println(mc.get(MessageContext.WSDL_OPERATION).toString());
            
            if (mc.get(MessageContext.WSDL_OPERATION).toString().contains("getRpcs")) {
                try {
//                    String cleanMessage = SoapHandlerUtils.cleanInvalidXmlChars(.toString(), "");
//                    System.out.println(cleanMessage);
//                    SoapHandlerUtils.getXmlMessage(mc.getMessage());
                } catch (Exception ex) {
                    Logger.getLogger(RawXMLHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                        
            return true;
        }
    }
}
