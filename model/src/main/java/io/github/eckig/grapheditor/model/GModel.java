/**
 */
package io.github.eckig.grapheditor.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GModel</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link io.github.eckig.grapheditor.model.GModel#getNodes <em>Nodes</em>}</li>
 *   <li>{@link io.github.eckig.grapheditor.model.GModel#getConnections <em>Connections</em>}</li>
 * </ul>
 *
 * @see io.github.eckig.grapheditor.model.GraphPackage#getGModel()
 * @model
 * @generated
 */
public interface GModel extends EObject {
	/**
	 * Returns the value of the '<em><b>Nodes</b></em>' containment reference list.
	 * The list contents are of type {@link io.github.eckig.grapheditor.model.GNode}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nodes</em>' containment reference list.
	 * @see io.github.eckig.grapheditor.model.GraphPackage#getGModel_Nodes()
	 * @model containment="true"
	 * @generated
	 */
	EList<GNode> getNodes();

	/**
	 * Returns the value of the '<em><b>Connections</b></em>' containment reference list.
	 * The list contents are of type {@link io.github.eckig.grapheditor.model.GConnection}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Connections</em>' containment reference list.
	 * @see io.github.eckig.grapheditor.model.GraphPackage#getGModel_Connections()
	 * @model containment="true"
	 * @generated
	 */
	EList<GConnection> getConnections();

} // GModel
