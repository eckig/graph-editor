/**
 */
package de.tesis.dynaware.grapheditor.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GModel</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getNodes <em>Nodes</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getConnections <em>Connections</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getContentWidth <em>Content Width</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getContentHeight <em>Content Height</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GModel#getSupergraph <em>Supergraph</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel()
 * @model
 * @generated
 */
public interface GModel extends EObject {
    /**
     * Returns the value of the '<em><b>Nodes</b></em>' containment reference list.
     * The list contents are of type {@link de.tesis.dynaware.grapheditor.model.GNode}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Nodes</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Nodes</em>' containment reference list.
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_Nodes()
     * @model containment="true"
     * @generated
     */
    EList<GNode> getNodes();

    /**
     * Returns the value of the '<em><b>Connections</b></em>' containment reference list.
     * The list contents are of type {@link de.tesis.dynaware.grapheditor.model.GConnection}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Connections</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Connections</em>' containment reference list.
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_Connections()
     * @model containment="true"
     * @generated
     */
    EList<GConnection> getConnections();

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
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_Type()
     * @model
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GModel#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

    /**
     * Returns the value of the '<em><b>Content Height</b></em>' attribute.
     * The default value is <code>"2250"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Content Height</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Content Height</em>' attribute.
     * @see #setContentHeight(double)
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_ContentHeight()
     * @model default="2250" required="true"
     * @generated
     */
    double getContentHeight();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GModel#getContentHeight <em>Content Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Content Height</em>' attribute.
     * @see #getContentHeight()
     * @generated
     */
    void setContentHeight(double value);

    /**
     * Returns the value of the '<em><b>Supergraph</b></em>' container reference.
     * It is bidirectional and its opposite is '{@link de.tesis.dynaware.grapheditor.model.GNode#getSubgraph <em>Subgraph</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Supergraph</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Supergraph</em>' container reference.
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_Supergraph()
     * @see de.tesis.dynaware.grapheditor.model.GNode#getSubgraph
     * @model opposite="subgraph" transient="false" changeable="false"
     * @generated
     */
    GNode getSupergraph();

    /**
     * Returns the value of the '<em><b>Content Width</b></em>' attribute.
     * The default value is <code>"3000"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Content Width</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Content Width</em>' attribute.
     * @see #setContentWidth(double)
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGModel_ContentWidth()
     * @model default="3000" required="true"
     * @generated
     */
    double getContentWidth();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GModel#getContentWidth <em>Content Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Content Width</em>' attribute.
     * @see #getContentWidth()
     * @generated
     */
    void setContentWidth(double value);

} // GModel
