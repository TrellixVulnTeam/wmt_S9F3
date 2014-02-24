/**
 * <License>
 */
package edu.colorado.csdms.wmt.client.ui;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;

import edu.colorado.csdms.wmt.client.data.ParameterJSO;

/**
 * Builds a table of parameters for a single WMT model component. The value of
 * the parameter is editable.
 * 
 * @author Mark Piper (mark.piper@colorado.edu)
 */
public class ParameterTable extends FlexTable {

  private DataManager data;
  private String componentId; // the id of the displayed component

  /**
   * Initializes a table of parameters for a single WMT model component. The
   * table is empty until {@link #loadTable()} is called.
   * 
   * @param data the DataManager instance for the WMT session
   */
  public ParameterTable(DataManager data) {

    this.data = data;
    this.data.setParameterTable(this);
    this.setWidth("100%");

    if (this.data.getSelectedComponent() != null) {
      loadTable();
    }
  }

  /**
   * A worker that loads the ParameterTable with parameter values for the
   * selected model component.
   */
  public void loadTable() {

    // The component whose parameters are to be displayed.
    this.setComponentId(data.getSelectedComponent());

    // Return if the selected component doesn't have parameters.
    if (data.getModelComponent(componentId).getParameters() == null) {
      this.clearTable();
      Window.alert("No parameters defined for this component.");
      return;
    }

    // Set the component name on the tab holding the ParameterTable.
    String componentName = data.getModelComponent(componentId).getName();
    String tabTitle = "Parameters (" + componentName + ")";
    data.getPerspective().getViewEast().setTabText(0, tabTitle);

    // Build the parameter table.
    Integer nParameters =
        data.getModelComponent(componentId).getParameters().length();
    Integer parameterIndex = 0;
    for (int i = 0; i < nParameters; i++) {
      ParameterJSO parameter =
          data.getModelComponent(componentId).getParameters().get(i);
      if (parameter.getKey().matches("simulation_name")) {
        continue;
      }
      this.setWidget(parameterIndex, 0, new DescriptionCell(parameter));
      if (parameter.getKey().matches("separator")) {
        this.getFlexCellFormatter().setColSpan(parameterIndex, 0, 2);
        this.getFlexCellFormatter().setStyleName(parameterIndex, 0,
            "wmt-ParameterSeparator");
      } else {
        this.setWidget(parameterIndex, 1, new ValueCell(parameter));
        this.getFlexCellFormatter().setStyleName(parameterIndex, 0,
            "wmt-ParameterDescription");
      }
      parameterIndex++;
    }
  }

  /**
   * Stores the modified value of a parameter of a model component in the WMT
   * {@link DataManager}.
   * 
   * @param parameter the ParameterJSO object for the parameter being modified
   * @param value the new parameter value, a String
   */
  public void setValue(ParameterJSO parameter, String value) {

    // TODO Massive amounts of checking on value.

    String key = parameter.getKey();
    String previousValue =
        data.getModelComponent(componentId).getParameter(key).getValue()
            .getDefault();
    GWT.log(componentId + ": " + key + ": " + value);

    // Don't update state when tabbing between fields or moving within field.
    // XXX Would be better to handle this further upstream.
    if (!value.matches(previousValue)) {
      data.getModelComponent(componentId).getParameter(key).getValue()
          .setDefault(value);
      data.modelIsSaved(false);
      data.getPerspective().setModelPanelTitle();
    }
  }

  /**
   * Deletes the contents of the ParameterTable and resets the tab title to
   * "Parameters". Unsets the selectedComponent in the DataManager.
   */
  public void clearTable() {
    data.setSelectedComponent(null); // should also be in ControlCell#delete?
    this.setComponentId(null);
    data.getPerspective().getViewEast().setTabText(0, "Parameters");
    this.removeAllRows();
    this.clear(true);
  }

  /**
   * Returns the id of the model component (a String) whose parameters are
   * displayed in the ParameterTable.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Stores the id of the model component (a String) whose parameters are
   * displayed in the ParameterTable.
   * 
   * @param componentId a component id, a String
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }
}
