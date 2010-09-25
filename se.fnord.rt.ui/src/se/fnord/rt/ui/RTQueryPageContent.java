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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RTQueryPageContent extends Composite {

    private Label queryLabel;
    private Text queryText;
    private Label titleLabel;
    private Text titleText;

    RTQueryPageContent(Composite parent, int style) {
        super(parent, style);
        createContents();
    }

    private Text createTextField() {
        GridData grid = new GridData();
        grid.grabExcessHorizontalSpace = true;
        grid.horizontalAlignment = GridData.FILL;

        Text text = new Text(this, SWT.BORDER);
        text.setLayoutData(grid);
        return text;
    }

    private Label createLabel(String labelText) {
        final Label label = new Label(this, SWT.NONE);
        label.setText(labelText);
        return label;
    }

    public void createContents() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;

        titleLabel = createLabel("Name:");
        titleText = createTextField();

        queryLabel = createLabel("Query:");
        queryText = createTextField();

        setLayout(layout);

    }

    public Label getQueryLabel() {
        return queryLabel;
    }

    public Text getQueryText() {
        return queryText;
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    public Text getTitleText() {
        return titleText;
    }

}
