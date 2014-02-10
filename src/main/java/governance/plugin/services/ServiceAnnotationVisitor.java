package governance.plugin.services;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * Created by jayanga on 2/9/14.
 */
public class ServiceAnnotationVisitor extends ASTVisitor {
    List<Object> serviceInfoList;

    public ServiceAnnotationVisitor(List<Object> serviceInfoList){
        this.serviceInfoList = serviceInfoList;
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        //System.out.println("====================MarkerAnnotation");
        //System.out.println(node.getTypeName().toString());

        if (node.getTypeName().toString().equals("WebService")){

            ASTNode n = node;
            while(n.getNodeType() != ASTNode.TYPE_DECLARATION){
                n = n.getParent();
            }

            String[] serviceInfo = {((TypeDeclaration) n).getName().getIdentifier(), "1.0.0", "JAX-WS", ""};
            serviceInfoList.add(serviceInfo);
        }

        return false;
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {
        //System.out.println("====================SingleMemberAnnotation");
        //System.out.println(node.getTypeName().toString());

        if (node.getTypeName().toString().equals("Path")){


            ASTNode n = node;
            while(n.getNodeType() != ASTNode.TYPE_DECLARATION){
                n = n.getParent();
            }

            String[] serviceInfo = {((TypeDeclaration) n).getName().getIdentifier(), "1.0.0", "JAX-RS", ""};
            serviceInfoList.add(serviceInfo);
        }

        return false;
    }
}
