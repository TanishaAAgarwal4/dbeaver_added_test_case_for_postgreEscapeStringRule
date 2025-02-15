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
package org.jkiss.dbeaver.ext.exasol.ui.config;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.jkiss.dbeaver.ext.exasol.model.ExasolDataSource;
import org.jkiss.dbeaver.ext.exasol.model.security.ExasolUser;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Map;

public class ExasolUserConfigurator implements DBEObjectConfigurator<ExasolUser> {
    @Override
    public ExasolUser configureObject(DBRProgressMonitor monitor, Object container, ExasolUser user, Map<String, Object> options) {
        return new UITask<ExasolUser>() {
            @Override
            protected ExasolUser runTask() {
                ExasolUserDialog dialog = new ExasolUserDialog(UIUtils.getActiveWorkbenchShell(), (ExasolDataSource) container);
                if (dialog.open() != IDialogConstants.OK_ID) {
                    return null;
                }
                user.setName(dialog.getName());
                user.setDescription(dialog.getComment());
                switch (dialog.getUserType()) {
                    case KERBEROS:
                        user.setKerberosPrincipal(dialog.getKerberosPrincipal());
                        break;
                    case LDAP:
                        user.setDN(dialog.getLDAPDN());
                        break;
                    case LOCAL:
                        user.setPassword(dialog.getPassword());
                        break;
                }
                return user;
            }
        }.execute();
    }
}
