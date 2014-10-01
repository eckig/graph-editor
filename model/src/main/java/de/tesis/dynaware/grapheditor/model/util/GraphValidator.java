/**
 */
package de.tesis.dynaware.grapheditor.model.util;

import de.tesis.dynaware.grapheditor.model.*;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EObjectValidator;

/**
 * <!-- begin-user-doc -->
 * The <b>Validator</b> for the model.
 * <!-- end-user-doc -->
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage
 * @generated
 */
public class GraphValidator extends EObjectValidator {
    /**
     * The cached model package
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static final GraphValidator INSTANCE = new GraphValidator();

    /**
     * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.common.util.Diagnostic#getSource()
     * @see org.eclipse.emf.common.util.Diagnostic#getCode()
     * @generated
     */
    public static final String DIAGNOSTIC_SOURCE = "de.tesis.dynaware.javafx.grapheditor.model";

    /**
     * A constant with a fixed name that can be used as the base value for additional hand written constants.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;

    /**
     * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;

    /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public GraphValidator() {
        super();
    }

    /**
     * Returns the package of this validator switch.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EPackage getEPackage() {
      return GraphPackage.eINSTANCE;
    }

    /**
     * Calls <code>validateXXX</code> for the corresponding classifier of the model.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
        switch (classifierID) {
            case GraphPackage.GMODEL:
                return validateGModel((GModel)value, diagnostics, context);
            case GraphPackage.GNODE:
                return validateGNode((GNode)value, diagnostics, context);
            case GraphPackage.GCONNECTOR:
                return validateGConnector((GConnector)value, diagnostics, context);
            case GraphPackage.GCONNECTION:
                return validateGConnection((GConnection)value, diagnostics, context);
            case GraphPackage.GJOINT:
                return validateGJoint((GJoint)value, diagnostics, context);
            case GraphPackage.GCONNECTABLE:
                return validateGConnectable((GConnectable)value, diagnostics, context);
            default:
                return true;
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGModel(GModel gModel, DiagnosticChain diagnostics, Map<Object, Object> context) {
        if (!validate_NoCircularContainment(gModel, diagnostics, context)) return false;
        boolean result = validate_EveryMultiplicityConforms(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryDataValueConforms(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryProxyResolves(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_UniqueID(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryKeyUnique(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validateGModel_isContentWidthValid(gModel, diagnostics, context);
        if (result || diagnostics != null) result &= validateGModel_isContentHeightValid(gModel, diagnostics, context);
        return result;
    }

    /**
     * The cached validation expression for the isContentWidthValid constraint of '<em>GModel</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final String GMODEL__IS_CONTENT_WIDTH_VALID__EEXPRESSION = "contentWidth >= 0";

    /**
     * Validates the isContentHeightValid constraint of '<em>GModel</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGModel_isContentHeightValid(GModel gModel, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return
            validate
                (GraphPackage.Literals.GMODEL,
                 gModel,
                 diagnostics,
                 context,
                 "http://www.eclipse.org/emf/2002/Ecore/OCL",
                 "isContentHeightValid",
                 GMODEL__IS_CONTENT_HEIGHT_VALID__EEXPRESSION,
                 Diagnostic.ERROR,
                 DIAGNOSTIC_SOURCE,
                 0);
    }

    /**
     * Validates the isContentWidthValid constraint of '<em>GModel</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGModel_isContentWidthValid(GModel gModel, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return
            validate
                (GraphPackage.Literals.GMODEL,
                 gModel,
                 diagnostics,
                 context,
                 "http://www.eclipse.org/emf/2002/Ecore/OCL",
                 "isContentWidthValid",
                 GMODEL__IS_CONTENT_WIDTH_VALID__EEXPRESSION,
                 Diagnostic.ERROR,
                 DIAGNOSTIC_SOURCE,
                 0);
    }

    /**
     * The cached validation expression for the isContentHeightValid constraint of '<em>GModel</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final String GMODEL__IS_CONTENT_HEIGHT_VALID__EEXPRESSION = "contentHeight >= 0";

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGNode(GNode gNode, DiagnosticChain diagnostics, Map<Object, Object> context) {
        if (!validate_NoCircularContainment(gNode, diagnostics, context)) return false;
        boolean result = validate_EveryMultiplicityConforms(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryDataValueConforms(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryProxyResolves(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_UniqueID(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryKeyUnique(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validateGNode_isWidthValid(gNode, diagnostics, context);
        if (result || diagnostics != null) result &= validateGNode_isHeightValid(gNode, diagnostics, context);
        return result;
    }

    /**
     * The cached validation expression for the isWidthValid constraint of '<em>GNode</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final String GNODE__IS_WIDTH_VALID__EEXPRESSION = "width >= 0";

    /**
     * Validates the isWidthValid constraint of '<em>GNode</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGNode_isWidthValid(GNode gNode, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return
            validate
                (GraphPackage.Literals.GNODE,
                 gNode,
                 diagnostics,
                 context,
                 "http://www.eclipse.org/emf/2002/Ecore/OCL",
                 "isWidthValid",
                 GNODE__IS_WIDTH_VALID__EEXPRESSION,
                 Diagnostic.ERROR,
                 DIAGNOSTIC_SOURCE,
                 0);
    }

    /**
     * The cached validation expression for the isHeightValid constraint of '<em>GNode</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static final String GNODE__IS_HEIGHT_VALID__EEXPRESSION = "height >= 0";

    /**
     * Validates the isHeightValid constraint of '<em>GNode</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGNode_isHeightValid(GNode gNode, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return
            validate
                (GraphPackage.Literals.GNODE,
                 gNode,
                 diagnostics,
                 context,
                 "http://www.eclipse.org/emf/2002/Ecore/OCL",
                 "isHeightValid",
                 GNODE__IS_HEIGHT_VALID__EEXPRESSION,
                 Diagnostic.ERROR,
                 DIAGNOSTIC_SOURCE,
                 0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGConnector(GConnector gConnector, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return validate_EveryDefaultConstraint(gConnector, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGConnection(GConnection gConnection, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return validate_EveryDefaultConstraint(gConnection, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGJoint(GJoint gJoint, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return validate_EveryDefaultConstraint(gJoint, diagnostics, context);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public boolean validateGConnectable(GConnectable gConnectable, DiagnosticChain diagnostics, Map<Object, Object> context) {
        return validate_EveryDefaultConstraint(gConnectable, diagnostics, context);
    }

    /**
     * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public ResourceLocator getResourceLocator() {
        // TODO
        // Specialize this to return a resource locator for messages specific to this validator.
        // Ensure that you remove @generated or mark it @generated NOT
        return super.getResourceLocator();
    }

} //GraphValidator
