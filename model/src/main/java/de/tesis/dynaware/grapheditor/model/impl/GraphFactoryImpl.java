/**
 */
package de.tesis.dynaware.grapheditor.model.impl;

import de.tesis.dynaware.grapheditor.model.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GraphFactoryImpl extends EFactoryImpl implements GraphFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static GraphFactory init() {
		try {
			GraphFactory theGraphFactory = (GraphFactory)EPackage.Registry.INSTANCE.getEFactory(GraphPackage.eNS_URI);
			if (theGraphFactory != null) {
				return theGraphFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new GraphFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GraphFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case GraphPackage.GMODEL: return createGModel();
			case GraphPackage.GNODE: return createGNode();
			case GraphPackage.GCONNECTION: return createGConnection();
			case GraphPackage.GCONNECTOR: return createGConnector();
			case GraphPackage.GJOINT: return createGJoint();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GModel createGModel() {
		GModelImpl gModel = new GModelImpl();
		return gModel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GNode createGNode() {
		GNodeImpl gNode = new GNodeImpl();
		return gNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GConnection createGConnection() {
		GConnectionImpl gConnection = new GConnectionImpl();
		return gConnection;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GConnector createGConnector() {
		GConnectorImpl gConnector = new GConnectorImpl();
		return gConnector;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GJoint createGJoint() {
		GJointImpl gJoint = new GJointImpl();
		return gJoint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GraphPackage getGraphPackage() {
		return (GraphPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static GraphPackage getPackage() {
		return GraphPackage.eINSTANCE;
	}

} //GraphFactoryImpl
