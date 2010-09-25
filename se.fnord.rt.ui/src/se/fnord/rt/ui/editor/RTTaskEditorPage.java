/*
 * Copyright (c) 2010 Henrik Gustafsson <henrik.gustafsson@fnord.se>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package se.fnord.rt.ui.editor;

import org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.forms.editor.IFormPage;

import se.fnord.rt.core.RequestTrackerRepositoryConnector;

@SuppressWarnings("restriction")
public class RTTaskEditorPage extends AbstractTaskEditorPage implements IFormPage {

    private AttributeEditorFactory editorFactory;

    private static final class RTAttributeEditorFactory extends AttributeEditorFactory {

        private TaskDataModel model;
        public RTAttributeEditorFactory(TaskDataModel model, TaskRepository taskRepository) {
            super(model, taskRepository);
            this.model = model;
        }
        @Override
        public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
            if (TaskAttribute.TYPE_INTEGER.equals(type))
                return new TextAttributeEditor(model, taskAttribute);
            return super.createEditor(type, taskAttribute);
        }
    }
    
    public RTTaskEditorPage(TaskEditor editor) {
        super(editor, "rtTaskEditorPage", RequestTrackerRepositoryConnector.REPOSITORY_TYPE_LABEL, RequestTrackerRepositoryConnector.REPOSITORY_CONNECTOR_KIND);
        setNeedsPrivateSection(false);
        setNeedsSubmit(true);
    }
    
    @Override
    public AttributeEditorFactory getAttributeEditorFactory() {
        if (super.getAttributeEditorFactory() != null && editorFactory == null)
            editorFactory = new RTAttributeEditorFactory(getModel(), getTaskRepository());

        return editorFactory;
    }
    
}
