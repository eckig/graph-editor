/**
 */
package eu.eckig.grapheditor.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GConnection</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link eu.eckig.grapheditor.model.GConnection#getId <em>Id</em>}</li>
 *   <li>{@link eu.eckig.grapheditor.model.GConnection#getType <em>Type</em>}</li>
 *   <li>{@link eu.eckig.grapheditor.model.GConnection#getSource <em>Source</em>}</li>
 *   <li>{@link eu.eckig.grapheditor.model.GConnection#getTarget <em>Target</em>}</li>
 *   <li>{@link eu.eckig.grapheditor.model.GConnection#getJoints <em>Joints</em>}</li>
 * </ul>
 *
 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection()
 * @model
 * @generated
 */
public interface GConnection extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection_Id()
	 * @model id="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link eu.eckig.grapheditor.model.GConnection#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link eu.eckig.grapheditor.model.GConnection#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' reference.
	 * @see #setSource(GConnector)
	 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection_Source()
	 * @model required="true"
	 * @generated
	 */
	GConnector getSource();

	/**
	 * Sets the value of the '{@link eu.eckig.grapheditor.model.GConnection#getSource <em>Source</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(GConnector value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(GConnector)
	 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection_Target()
	 * @model required="true"
	 * @generated
	 */
	GConnector getTarget();

	/**
	 * Sets the value of the '{@link eu.eckig.grapheditor.model.GConnection#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(GConnector value);

	/**
	 * Returns the value of the '<em><b>Joints</b></em>' containment reference list.
	 * The list contents are of type {@link eu.eckig.grapheditor.model.GJoint}.
	 * It is bidirectional and its opposite is '{@link eu.eckig.grapheditor.model.GJoint#getConnection <em>Connection</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Joints</em>' containment reference list.
	 * @see eu.eckig.grapheditor.model.GraphPackage#getGConnection_Joints()
	 * @see eu.eckig.grapheditor.model.GJoint#getConnection
	 * @model opposite="connection" containment="true"
	 * @generated
	 */
	EList<GJoint> getJoints();

} // GConnection
