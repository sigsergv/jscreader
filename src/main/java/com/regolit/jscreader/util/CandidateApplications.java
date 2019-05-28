/* INSERT LICENSE HERE */

package com.regolit.jscreader.util;

import com.regolit.jscreader.model.ApplicationInfoModel;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathEvaluationResult;
import org.w3c.dom.NodeList;
// import javax.xml.parsers.DocumentBuilder;


public class CandidateApplications {
    private static CandidateApplications instance = null;
    private List<ApplicationInfoModel> presetApps;
    private List<ApplicationInfoModel> apps = null;

    private CandidateApplications() {
        presetApps = new ArrayList<ApplicationInfoModel>(100);
        // apps = new ArrayList<ApplicationInfoModel>(100);

        // load default applications list from resource XML
        try {
            var dbf = DocumentBuilderFactory.newInstance();
            var db = dbf.newDocumentBuilder();
            var stream = CandidateApplications.class.getResourceAsStream("/candidate_aid_list.xml");
            var doc = db.parse(stream);
            var xpath = XPathFactory.newInstance().newXPath();
            var appNodes = (NodeList)xpath.evaluate("/applications/application", doc, XPathConstants.NODESET);
            var nodesCount = appNodes.getLength();
            for (int i=0; i<nodesCount; i++) {
                var node = appNodes.item(i);
                if (node == null) {
                    break;
                }
                var attrs = node.getAttributes();
                boolean enabled = false;

                var enabledNode = attrs.getNamedItem("enabled");
                if (enabledNode != null && enabledNode.getNodeValue().equals("true")) {
                    enabled = true;
                }
                var aidNode = attrs.getNamedItem("aid");
                if (aidNode == null) {
                    continue;
                }
                var typeNode = attrs.getNamedItem("type");
                if (aidNode == null) {
                    continue;
                }
                var nameNode = attrs.getNamedItem("name");
                var name = "Unknown";
                if (aidNode != null) {
                    name = nameNode.getNodeValue();
                }
                var m = new ApplicationInfoModel(aidNode.getNodeValue(), typeNode.getNodeValue(), name, enabled);
                presetApps.add(m);
            }

        } catch (javax.xml.xpath.XPathExpressionException e) {
            System.err.printf("Failed to parse XPATH \"/candidate_aid_list.xml\": %s\n", e);
        } catch (java.io.IOException e) {
            System.err.printf("Failed to parse XML \"/candidate_aid_list.xml\", IOException: %s\n", e);
        } catch (org.xml.sax.SAXException e) {
            System.err.printf("Failed to parse XML \"/candidate_aid_list.xml\": %s\n", e);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            System.err.printf("Failed to parse XML \"/candidate_aid_list.xml\"\n");
        }
    }

    public static CandidateApplications getInstance() {
        if (instance == null) {
            instance = new CandidateApplications();
        }
        return instance;
    }

    public List<ApplicationInfoModel> list() {
        return java.util.Collections.unmodifiableList​(presetApps);
    }
}