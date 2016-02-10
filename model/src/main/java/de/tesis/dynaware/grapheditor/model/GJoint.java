/**
 */
package de.tesis.dynaware.grapheditor.model;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GJoint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GJoint#getId <em>Id</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GJoint#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GJoint#getConnection <em>Connection</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GJoint#getX <em>X</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GJoint#getY <em>Y</em>}</li>
 * </ul>
 *
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint()
 * @model
 * @generated
 */
public interface GJoint extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint_Id()
	 * @model id="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GJoint#getId <em>Id</em>}' attribute.
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
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GJoint#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Connection</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link de.tesis.dynaware.grapheditor.model.GConnection#getJoints <em>Joints</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Connection</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Connection</em>' container reference.
	 * @see #setConnection(GConnection)
	 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint_Connection()
	 * @see de.tesis.dynaware.grapheditor.model.GConnection#getJoints
	 * @model opposite="joints" required="true" transient="false"
	 * @generated
	 */
	GConnection getConnection();

	/**
	 * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GJoint#getConnection <em>Connection</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Connection</em>' container reference.
	 * @see #getConnection()
	 * @generated
	 */
	void setConnection(GConnection value);

	/**
	 * Returns the value of the '<em><b>X</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>X</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>X</em>' attribute.
	 * @see #setX(double)
	 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint_X()
	 * @model default="0" required="true"
	 * @generated
	 */
	double getX();

	/**
	 * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GJoint#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>X</em>' attribute.
	 * @see #getX()
	 * @generated
	 */
	void setX(double value);

	/**
	 * Returns the value of the '<em><b>Y</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Y</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Y</em>' attribute.
	 * @see #setY(double)
	 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGJoint_Y()
	 * @model default="0" required="true"
	 * @generated
	 */
	double getY();

	/**
	 * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GJoint#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Y</em>' attribute.
	 * @see #getY()
	 * @generated
	 */
	void setY(double value);

} // GJoint
