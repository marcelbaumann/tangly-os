/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.PasswordField;
import net.tangly.erp.crm.domain.NaturalEntity;
import net.tangly.erp.crm.domain.Subject;
import org.junit.jupiter.api.Test;

import static com.github.mvysny.kaributesting.v10.LocatorJ.*;
import static net.tangly.erp.crm.ui.CmdChangePassword.*;
import static org.assertj.core.api.Assertions.assertThat;

class CmdChangePasswordItTest extends CrmItTest {
    @Test
    void testChangePasswordCorrectly() {
        var subject = createSubject();
        var domain = ofDomain();
        domain.realm().subjects().update(subject);
        var cmd = new CmdChangePassword(domain, subject);
        var form = cmd.form();
        assertThat(form).isNotNull();
        assertThat(subject.authenticate("old-password")).isTrue();
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CURRENT_PASSWORD)), "old-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withCaption(EXECUTE)));
        assertThat(subject.authenticate("new-password")).isTrue();
    }

    @Test
    void testChangePasswordWrongly() {
        var subject = createSubject();
        var domain = ofDomain();
        domain.realm().subjects().update(subject);
        var cmd = new CmdChangePassword(domain, subject);
        var form = cmd.form();
        assertThat(subject.authenticate("old-password")).isTrue();
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withCaption(EXECUTE)));
        assertThat(subject.authenticate("old-password")).isTrue();
        assertThat(subject.authenticate("new-password")).isFalse();
    }

    @Test
    void testChangePasswordCanceled() {
        var subject = createSubject();
        var domain = ofDomain();
        domain.realm().subjects().update(subject);
        var cmd = new CmdChangePassword(domain, subject);
        var form = cmd.form();
        assertThat(subject.authenticate("old-password")).isTrue();
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CURRENT_PASSWORD)), "dummy-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(NEW_PASSWORD)), "new-password");
        _setValue(_get(form, PasswordField.class, spec -> spec.withCaption(CONFIRM_PASSWORD)), "new-password");
        _click(_get(form, Button.class, spec -> spec.withCaption(CANCEL)));
        assertThat(subject.authenticate("old-password")).isTrue();
        assertThat(subject.authenticate("new-password")).isFalse();
    }

    private Subject createSubject() {
        var person = new NaturalEntity();
        person.firstname("John");
        person.lastname("Doe");
        var subject = new Subject();
        subject.user(person);
        subject.id("john-doe");
        subject.newPassword("old-password");
        return subject;
    }
}
