/**
 */
package de.tesis.dynaware.grapheditor.model;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>GNode</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getId <em>Id</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getType <em>Type</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getX <em>X</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getY <em>Y</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getWidth <em>Width</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getHeight <em>Height</em>}</li>
 *   <li>{@link de.tesis.dynaware.grapheditor.model.GNode#getSubgraph <em>Subgraph</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode()
 * @model
 * @generated
 */
public interface GNode extends GConnectable {
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
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Id()
     * @model id="true"
     * @generated
     */
    String getId();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getId <em>Id</em>}' attribute.
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
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Type()
     * @model
     * @generated
     */
    String getType();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getType <em>Type</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Type</em>' attribute.
     * @see #getType()
     * @generated
     */
    void setType(String value);

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
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_X()
     * @model default="0" required="true"
     * @generated
     */
    double getX();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getX <em>X</em>}' attribute.
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
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Y()
     * @model default="0" required="true"
     * @generated
     */
    double getY();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getY <em>Y</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Y</em>' attribute.
     * @see #getY()
     * @generated
     */
    void setY(double value);

    /**
     * Returns the value of the '<em><b>Width</b></em>' attribute.
     * The default value is <code>"151"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Width</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Width</em>' attribute.
     * @see #setWidth(double)
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Width()
     * @model default="151" required="true"
     * @generated
     */
    double getWidth();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getWidth <em>Width</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Width</em>' attribute.
     * @see #getWidth()
     * @generated
     */
    void setWidth(double value);

    /**
     * Returns the value of the '<em><b>Height</b></em>' attribute.
     * The default value is <code>"101"</code>.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Height</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Height</em>' attribute.
     * @see #setHeight(double)
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Height()
     * @model default="101" required="true"
     * @generated
     */
    double getHeight();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getHeight <em>Height</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Height</em>' attribute.
     * @see #getHeight()
     * @generated
     */
    void setHeight(double value);

    /**
     * Returns the value of the '<em><b>Subgraph</b></em>' containment reference.
     * It is bidirectional and its opposite is '{@link de.tesis.dynaware.grapheditor.model.GModel#getSupergraph <em>Supergraph</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Subgraph</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Subgraph</em>' containment reference.
     * @see #setSubgraph(GModel)
     * @see de.tesis.dynaware.grapheditor.model.GraphPackage#getGNode_Subgraph()
     * @see de.tesis.dynaware.grapheditor.model.GModel#getSupergraph
     * @model opposite="supergraph" containment="true"
     * @generated
     */
    GModel getSubgraph();

    /**
     * Sets the value of the '{@link de.tesis.dynaware.grapheditor.model.GNode#getSubgraph <em>Subgraph</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Subgraph</em>' containment reference.
     * @see #getSubgraph()
     * @generated
     */
    void setSubgraph(GModel value);

} // GNode
