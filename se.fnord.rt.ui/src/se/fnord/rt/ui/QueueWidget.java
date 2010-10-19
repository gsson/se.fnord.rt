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
package se.fnord.rt.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

public class QueueWidget extends Composite {
    private DataBindingContext m_bindingContext;

    private List<Queue> queues = new ArrayList<Queue>();

    private Table table;
    private TableViewer tableViewer;
    private TableViewerColumn idColumn;
    private Text text;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public QueueWidget(Composite parent, int style) {
        super(parent, style);
        setLayout(new FormLayout());

        tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
        tableViewer.setColumnProperties(new String[] {});
        table = tableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        FormData fd_table = new FormData();
        fd_table.bottom = new FormAttachment(100, -6);
        fd_table.left = new FormAttachment(0, 6);
        fd_table.right = new FormAttachment(75);
        table.setLayoutData(fd_table);

        idColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnId = idColumn.getColumn();
        tblclmnId.setWidth(30);
        tblclmnId.setText("Id");

        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnQueueName = nameColumn.getColumn();
        tblclmnQueueName.setWidth(100);
        tblclmnQueueName.setText("Name");

        TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        TableColumn tblclmnDescription = descriptionColumn.getColumn();
        tblclmnDescription.setWidth(100);
        tblclmnDescription.setText("Description");

        text = new Text(this, SWT.BORDER);
        fd_table.top = new FormAttachment(0, 31);
        FormData fd_text = new FormData();
        fd_text.bottom = new FormAttachment(table, -6);
        fd_text.right = new FormAttachment(table, 0, SWT.RIGHT);
        fd_text.left = new FormAttachment(0, 6);
        text.setLayoutData(fd_text);

        Button btnAddId = new Button(this, SWT.NONE);
        btnAddId.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    final Queue queue = new Queue();
                    queue.setId(Integer.parseInt(text.getText()));
                    queues.add(queue);
                    tableViewer.refresh();
                }
                catch (NumberFormatException f) {

                }
            }
        });
        FormData fd_btnAddId = new FormData();
        fd_btnAddId.left = new FormAttachment(text, 6);
        fd_btnAddId.right = new FormAttachment(100, -6);
        btnAddId.setLayoutData(fd_btnAddId);
        btnAddId.setText("Add Id");

        Button btnAddName = new Button(this, SWT.NONE);
        btnAddName.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Queue queue = new Queue();
                queue.setName(text.getText());
                queues.add(queue);
                tableViewer.refresh();
            }
        });
        FormData fd_btnAddName = new FormData();
        fd_btnAddName.left = new FormAttachment(table, 6);
        fd_btnAddName.right = new FormAttachment(100, -6);
        fd_btnAddName.top = new FormAttachment(btnAddId, 6);
        btnAddName.setLayoutData(fd_btnAddName);
        btnAddName.setText("Add Name");

        Button btnRemove = new Button(this, SWT.NONE);
        btnRemove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                for (Queue o : (List<Queue>) selection.toList())
                    queues.remove(o);
                tableViewer.refresh();
            }
        });
        btnRemove.setText("Remove");
        FormData fd_btnRemove = new FormData();
        fd_btnRemove.top = new FormAttachment(btnAddName, 6);
        fd_btnRemove.left = new FormAttachment(table, 6);
        fd_btnRemove.right = new FormAttachment(100, -6);
        btnRemove.setLayoutData(fd_btnRemove);

        m_bindingContext = initDataBindings();


    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        //
        ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
        tableViewer.setContentProvider(listContentProvider);
        //
        IObservableMap[] observeMaps = PojoObservables.observeMaps(listContentProvider.getKnownElements(), Queue.class, new String[]{"id", "name", "description"});
        tableViewer.setLabelProvider(new ObservableMapLabelProvider(observeMaps));
        //
        WritableList writableList = new WritableList(queues, Queue.class);
        tableViewer.setInput(writableList);
        //
        return bindingContext;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public void refresh() {
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                tableViewer.refresh();
            }
        });
    }
}
