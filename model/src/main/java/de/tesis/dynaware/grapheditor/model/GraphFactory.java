/**
 */
package de.tesis.dynaware.grapheditor.model;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage
 * @generated
 */
public interface GraphFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	GraphFactory eINSTANCE = de.tesis.dynaware.grapheditor.model.impl.GraphFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>GModel</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GModel</em>'.
	 * @generated
	 */
	GModel createGModel();

	/**
	 * Returns a new object of class '<em>GNode</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GNode</em>'.
	 * @generated
	 */
	GNode createGNode();

	/**
	 * Returns a new object of class '<em>GConnection</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GConnection</em>'.
	 * @generated
	 */
	GConnection createGConnection();

	/**
	 * Returns a new object of class '<em>GConnector</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GConnector</em>'.
	 * @generated
	 */
	GConnector createGConnector();

	/**
	 * Returns a new object of class '<em>GJoint</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>GJoint</em>'.
	 * @generated
	 */
	GJoint createGJoint();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	GraphPackage getGraphPackage();

} //GraphFactory
