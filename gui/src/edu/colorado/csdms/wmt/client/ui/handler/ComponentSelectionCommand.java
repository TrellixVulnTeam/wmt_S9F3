/**
 * <License>
 */
package edu.colorado.csdms.wmt.client.ui.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;

import edu.colorado.csdms.wmt.client.control.DataManager;
import edu.colorado.csdms.wmt.client.ui.ComponentActionMenu;
import edu.colorado.csdms.wmt.client.ui.ComponentCell;

/**
 * Defines the action for when a user selects a component in a
 * {@link ComponentCell}; adds or sets a component in the ModelTree and sets up
 * the {@link ComponentActionMenu}.
 * 
 * @author Mark Piper (mark.piper@colorado.edu)
 */
public class ComponentSelectionCommand implements Command {

  private DataManager data;
  private ComponentCell cell;
  private String componentId;
  
  /**
   * Creates a new instance of {@link ComponentSelectionCommand}.
   * 
   * @param data the DataManager object for the WMT session
   * @param cell the {@link ComponentCell} this Command acts on
   * @param componentId the id of the selected component
   */
  public ComponentSelectionCommand(DataManager data, ComponentCell cell,
      String componentId) {
    this.data = data;
    this.cell = cell;
    this.componentId = componentId;
  }

  @Override
  public void execute() {
    updateCell(false);
  }
  
  public void execute(Boolean useSetComponent) {
    updateCell(useSetComponent);
  }
  
  /**
   * A worker which allows a choice in {@link #execute()} of <em>adding</em> a
   * component (which adds a new TreeItem), or <em>setting</em> a component
   * (which uses an existing TreeItem).
   *  
   * @param useSetComponent if true, use ModelTree#setComponent
   */
  private void updateCell(Boolean useSetComponent) {
    
    // Tell the ComponentCell what component it now holds.
    cell.setComponentId(componentId);
    String componentName = data.getComponent(componentId).getName();
    GWT.log("Selected component: " + componentName);
    
    // Display the name of the selected component.
    String displayName = cell.trimName(componentName);
    cell.getComponentMenu().getComponentItem().setText(displayName);
    cell.getComponentMenu().getComponentItem().addStyleDependentName("connected");

    // Add/set the component to/on the ModelTree.
    if (useSetComponent) {
      data.getPerspective().getModelTree().setComponent(componentId,
        cell.getEnclosingTreeItem());
    } else {
      data.getPerspective().getModelTree().addComponent(componentId,
        cell.getEnclosingTreeItem());
    }

    // Replace the componentMenu with the actionMenu.
    ComponentActionMenu actionMenu = new ComponentActionMenu(data, cell);
    cell.getComponentMenu().getComponentItem().setSubMenu(actionMenu);
  }
}