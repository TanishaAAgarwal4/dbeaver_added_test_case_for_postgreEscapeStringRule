/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jkiss.dbeaver.ui;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.commands.ToggleState;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.utils.CommonUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * Action utils
 */
public class ActionUtils
{
    private static final Log log = Log.getLog(ActionUtils.class);
    
    private static final Set<IPropertyChangeListener> propertyEvaluationRequestListeners = Collections.synchronizedSet(new HashSet<>());
    
    public static void addPropertyEvaluationRequestListener(@NotNull IPropertyChangeListener listener) {
        propertyEvaluationRequestListeners.add(listener);
    }

    public static void removePropertyEvaluationRequestListener(@NotNull IPropertyChangeListener listener) {
        propertyEvaluationRequestListeners.remove(listener);
    }

    public static CommandContributionItem makeCommandContribution(@NotNull IServiceLocator serviceLocator, @NotNull String commandId)
    {
        return makeCommandContribution(serviceLocator, commandId, CommandContributionItem.STYLE_PUSH);
    }

    public static CommandContributionItem makeCommandContribution(@NotNull IServiceLocator serviceLocator, @NotNull String commandId, int style)
    {
        return new CommandContributionItem(new CommandContributionItemParameter(
            serviceLocator,
            null,
            commandId,
            style));
    }

    public static CommandContributionItem makeCommandContribution(@NotNull IServiceLocator serviceLocator, @NotNull String commandId, int style, @Nullable  DBPImage icon)
    {
        CommandContributionItemParameter parameters = new CommandContributionItemParameter(
            serviceLocator,
            null,
            commandId,
            style);
        parameters.icon = DBeaverIcons.getImageDescriptor(icon);
        return new CommandContributionItem(parameters);
    }

    public static CommandContributionItem makeCommandContribution(IServiceLocator serviceLocator, String commandId, String name, DBPImage image)
    {
        return makeCommandContribution(serviceLocator, commandId, name, image, null, false);
    }

    public static ContributionItem makeActionContribution(
        @NotNull IAction action,
        boolean showText)
    {
        ActionContributionItem item = new ActionContributionItem(action);
        if (showText) {
            item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
        }
        return item;
    }

    public static ContributionItem makeActionContribution(
        @NotNull IAction action,
        DBPImage image)
    {
        ActionContributionItem item = new ActionContributionItem(action);
        if (image != null) {
            action.setImageDescriptor(DBeaverIcons.getImageDescriptor(image));
        }
        return item;
    }

    public static CommandContributionItem makeCommandContribution(
        @NotNull IServiceLocator serviceLocator,
        @NotNull String commandId,
        @Nullable String name,
        @Nullable DBPImage image,
        @Nullable String toolTip,
        boolean showText) {
        return makeCommandContribution(serviceLocator, commandId, CommandContributionItem.STYLE_PUSH, name, image, toolTip, showText, null);
    }

    public static CommandContributionItem makeCommandContribution(
        @NotNull IServiceLocator serviceLocator,
        @NotNull String commandId,
        int style,
        @Nullable String name,
        @Nullable DBPImage image,
        @Nullable String toolTip,
        boolean showText,
        @Nullable Map<String, Object> parameters)
    {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
            serviceLocator,
            null,
            commandId,
            null,
            image == null ? null : DBeaverIcons.getImageDescriptor(image),
            null,
            null,
            name,
            null,
            toolTip,
            style,
            null,
            false);
        contributionParameters.parameters = parameters;
        if (showText) {
            contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
        }
        return new CommandContributionItem(contributionParameters);
    }

    public static boolean isCommandEnabled(String commandId, IServiceLocator site)
    {
        if (commandId != null && site != null) {
            try {
                //Command cmd = new Command();
                ICommandService commandService = site.getService(ICommandService.class);
                if (commandService != null) {
                    Command command = commandService.getCommand(commandId);
                    return command != null && command.isEnabled();
                }
            } catch (Exception e) {
                log.error("Can't execute command '" + commandId + "'", e);
            }
        }
        return false;
    }

    public static boolean isCommandChecked(String commandId, IWorkbenchPartSite site)
    {
        if (commandId != null && site != null) {
            try {
                //Command cmd = new Command();
                ICommandService commandService = site.getService(ICommandService.class);
                if (commandService != null) {
                    Command command = commandService.getCommand(commandId);
                    return command != null && command.getState(ToggleState.class.getName()) != null;
                }
            } catch (Exception e) {
                log.error("Can't execute command '" + commandId + "'", e);
            }
        }
        return false;
    }

    @Nullable
    public static String findCommandName(String commandId)
    {
        ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService != null) {
            Command command = commandService.getCommand(commandId);
            if (command != null && command.isDefined()) {
                try {
                    return command.getName();
                } catch (NotDefinedException e) {
                    log.debug(e);
                }
            }
        }
        return "???";
    }

    @Nullable
    public static ImageDescriptor findCommandImage(String commandId)
    {
        ICommandImageService commandService = PlatformUI.getWorkbench().getService(ICommandImageService.class);
        if (commandService != null) {
            return commandService.getImageDescriptor(commandId);
        }
        return null;
    }

    @Nullable
    public static String findCommandDescription(String commandId, IServiceLocator serviceLocator, boolean shortcutOnly)
    {
        return findCommandDescription(commandId, serviceLocator, shortcutOnly, null, null);
    }

    @Nullable
    public static String findCommandDescription(String commandId, IServiceLocator serviceLocator, boolean shortcutOnly, String paramName, String paramValue)
    {
        String commandName = null;
        String shortcut = null;
        ICommandService commandService = serviceLocator.getService(ICommandService.class);
        if (commandService != null) {
            Command command = commandService.getCommand(commandId);
            if (command != null && command.isDefined()) {
                try {
                    commandName = command.getName();
                } catch (NotDefinedException e) {
                    log.debug(e);
                }
            }
        }
        IBindingService bindingService = serviceLocator.getService(IBindingService.class);
        if (bindingService != null) {
            TriggerSequence sequence = null;
            Binding[] bindings = bindingService.getBindings();
            if (bindings != null) {
                for (Binding b : bindings) {
                    ParameterizedCommand parameterizedCommand = b.getParameterizedCommand();
                    if (parameterizedCommand != null && commandId.equals(parameterizedCommand.getId())) {
                        if (paramName != null) {
                            Object cmdParamValue = parameterizedCommand.getParameterMap().get(paramName);
                            if (!CommonUtils.equalObjects(cmdParamValue, paramValue)) {
                                continue;
                            }
                        }
                        sequence = b.getTriggerSequence();
                        if (b.getType() == Binding.USER) {
                            // Prefer user-defined binding over default (system)
                            break;
                        }
                    }
                }
            }
            if (sequence == null) {
                sequence = bindingService.getBestActiveBindingFor(commandId);
            }
            if (sequence != null) {
                shortcut = sequence.format();
            }
        }
        if (shortcutOnly) {
            return shortcut == null ? "" : shortcut;
        }
        if (shortcut == null) {
            return commandName;
        }
        if (commandName == null) {
            return shortcut;
        }
        return commandName + " (" + shortcut + ")";
    }

    @Nullable
    public static Command findCommand(@NotNull String commandId) {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService != null) {
            final Command command = commandService.getCommand(commandId);
            if (command != null && command.isDefined()) {
                return command;
            }
        }
        return null;
    }

    public static void runCommand(String commandId, IServiceLocator serviceLocator)
    {
        runCommand(commandId, null, serviceLocator);
    }

    public static void runCommand(String commandId, ISelection selection, IServiceLocator serviceLocator)
    {
        runCommand(commandId, selection, null, serviceLocator);
    }

    public static void runCommand(String commandId, ISelection selection, Map<String, Object> parameters, IServiceLocator serviceLocator)
    {
        if (commandId != null) {
            try {
                ICommandService commandService = serviceLocator.getService(ICommandService.class);
                IHandlerService handlerService = serviceLocator.getService(IHandlerService.class);
                if (commandService != null) {
                    Command command = commandService.getCommand(commandId);
                    boolean needContextPatch = false;
                    if (selection != null) {
                        needContextPatch = true;
                        if (serviceLocator instanceof IWorkbenchPartSite) {
                            final ISelection curSelection = ((IWorkbenchSite) serviceLocator).getSelectionProvider().getSelection();
                            if (curSelection instanceof IStructuredSelection && selection instanceof IStructuredSelection) {
                                if (((IStructuredSelection) curSelection).size() == ((IStructuredSelection) selection).size() &&
                                    ((IStructuredSelection) curSelection).getFirstElement() == ((IStructuredSelection) selection).getFirstElement())
                                {
                                    // The same selection
                                    needContextPatch = false;
                                }
                            }
                        }
                    }

                    Parameterization[] parametrization = null;

                    if (!CommonUtils.isEmpty(parameters)) {
                        parametrization = new Parameterization[parameters.size()];
                        int paramIndex = 0;
                        for (Map.Entry<String, Object> param : parameters.entrySet()) {
                            IParameter parameter = command.getParameter(param.getKey());
                            if (parameter != null) {
                                parametrization[paramIndex] = new Parameterization(parameter, CommonUtils.toString(param.getValue()));
                            } else {
                                log.debug("Parameter '" + param.getKey() + "' not found in command '" + commandId + "'");
                                parametrization[paramIndex] = null;
                            }
                            paramIndex++;
                        }
                    }

                    if (selection != null && needContextPatch) {
                        // Create new eval context
                        IEvaluationContext context = new EvaluationContext(
                            handlerService.createContextSnapshot(false), selection);
                        if (serviceLocator instanceof IWorkbenchPartSite) {
                            context.addVariable(ISources.ACTIVE_PART_NAME, ((IWorkbenchPartSite) serviceLocator).getPart());
                        }
                        context.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);

                        ParameterizedCommand pc = new ParameterizedCommand(command, parametrization);
                        handlerService.executeCommandInContext(pc, null, context);
                    } else if (command != null) {
                        if (command.isEnabled()) {
                            ParameterizedCommand pc = new ParameterizedCommand(command, parametrization);
                            handlerService.executeCommand(pc, null);
                        } else {
                            log.warn("Command '" + commandId + "' is disabled");
                        }
                    } else {
                        log.warn("Command '" + commandId + "' not found");
                    }
                }
            } catch (Exception e) {
                DBWorkbench.getPlatformUI().showError("Error running command", "Can't execute command '" + commandId + "'", e);
            }
        }
    }

    public static IAction makeAction(
        @NotNull final IActionDelegate actionDelegate,
        @Nullable IWorkbenchSite site,
        @Nullable ISelection selection,
        @Nullable String id,
        @Nullable String text,
        @Nullable ImageDescriptor image,
        @Nullable String toolTip)
    {
        Action actionImpl = new Action() {
            @Override
            public void run() {
                actionDelegate.run(this);
            }
        };
        if (id != null) {
            actionImpl.setId(id);
        }
        if (text != null) {
            actionImpl.setText(text);
        }
        if (image != null) {
            actionImpl.setImageDescriptor(image);
        }
        if (toolTip != null) {
            actionImpl.setToolTipText(toolTip);
        }

        actionDelegate.selectionChanged(actionImpl, selection);

        if (site != null) {
            if (actionDelegate instanceof IObjectActionDelegate && site instanceof IWorkbenchPartSite) {
                ((IObjectActionDelegate)actionDelegate).setActivePart(actionImpl, ((IWorkbenchPartSite) site).getPart());
            } else if (actionDelegate instanceof IWorkbenchWindowActionDelegate) {
                ((IWorkbenchWindowActionDelegate)actionDelegate).init(site.getWorkbenchWindow());
            }
        }

        return actionImpl;
    }

    public static void evaluatePropertyState(String propertyName)
    {
        IEvaluationService service = PlatformUI.getWorkbench().getService(IEvaluationService.class);
        if (service != null) {
            try {
                service.requestEvaluation(propertyName);
            } catch (Exception e) {
                log.warn("Error evaluating property [" + propertyName + "]");
            }
        }
        
        PropertyChangeEvent ev = new PropertyChangeEvent(service, propertyName, null, null); 
        for (IPropertyChangeListener listener : List.copyOf(propertyEvaluationRequestListeners)) {
            listener.propertyChange(ev);
        }
    }

    public static void fireCommandRefresh(final String ... commandIDs)
    {
        // Update commands
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        if (commandService != null) {
            UIUtils.asyncExec(() -> {
                for (String commandID : commandIDs) {
                    commandService.refreshElements(commandID, null);
                }
            });
        }
    }
}
