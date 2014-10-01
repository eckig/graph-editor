/**
 */
package de.tesis.dynaware.grapheditor.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GConnectable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GConnectable#getConnectors <em>Connectors</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGConnectable()
 * @model abstract="true"
 * @generated
 */
public interface GConnectable extends EObject {
    /**
     * Returns the value of the '<em><b>Connectors</b></em>' containment reference list.
     * The list contents are of type {@link de.tesis.dynaware.grapheditor.model.GConnector}.
     * It is bidirectional and its opposite is '{@link de.tesis.dynaware.grapheditor.model.GConnector#getParent <em>Parent</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Connectors</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Connectors</em>' containment reference list.
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGConnectable_Connectors()
     * @see de.tesis.dynaware.grapheditor.model.GConnector#getParent
     * @model opposite="parent" containment="true"
     * @generated
     */
    EList<GConnector> getConnectors();

} // GConnectable
