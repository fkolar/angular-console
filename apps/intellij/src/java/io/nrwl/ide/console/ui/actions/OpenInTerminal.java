package io.nrwl.ide.console.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.impl.ToolWindowImpl;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.jediterm.terminal.ui.TerminalSession;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import io.nrwl.ide.console.ui.NgConsoleUI;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class OpenInTerminal extends AnAction {
  private static final Logger LOG = Logger.getInstance(OpenInTerminal.class);


  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {

    NgConsoleUI consoleUI = ServiceManager.getService(e.getProject(), NgConsoleUI.class);
    List<DOMElement> elements = consoleUI.querySelector(".window-header.command");


    if (elements != null && elements.size() == 1) {
      Project project = getEventProject(e);
      String ngCommand = elements.get(0).getTextContent().trim();

      ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
      if (window != null && window.isAvailable()) {
        ((ToolWindowImpl) window).ensureContentInitialized();
        window.activate(null);
      }
      ApplicationManager.getApplication().invokeLater(() -> {
        ContentManager terminal = ToolWindowManager.getInstance(project).getToolWindow("Terminal").getContentManager();
        Content content = terminal.getContent(0);
        JBTerminalWidget widget = (JBTerminalWidget) content.getComponent().getComponent(0);
        TerminalSession currentSession = widget.getCurrentSession();

        try {
          currentSession.getTtyConnector().write(ngCommand);
        } catch (IOException ex) {
          LOG.error("Problem while writing into terminal: ", ex);
        }
      });
    }
  }


  /**
   * Checks 2 dom elements to see if we can show the button
   */
  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    ApplicationManager.getApplication().invokeLater(() -> {
      NgConsoleUI consoleUI = ServiceManager.getService(e.getProject(), NgConsoleUI.class);
      List<DOMElement> commands = consoleUI.querySelector(".window-header.command");
      List<DOMElement> genButton = consoleUI.querySelector(".actions-container .action-button ");

      boolean gbEnabled = genButton != null && genButton.size() == 1 && !genButton.get(0).hasAttribute("disabled");

      e.getPresentation().setEnabledAndVisible(commands != null && commands.size() > 0 && gbEnabled);
    });
  }


}
