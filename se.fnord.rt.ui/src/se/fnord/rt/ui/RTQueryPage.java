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

import java.net.MalformedURLException;

import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import se.fnord.rt.core.RequestTrackerTaskDataHandler;
import se.fnord.rt.core.internal.URLFactory;

public class RTQueryPage extends AbstractRepositoryQueryPage {

    private RTQueryPageContent content;
    private String queryString = "";
    private String titleString = "";
    private final URLFactory urls;

    public RTQueryPage(TaskRepository taskRepository, IRepositoryQuery query) {
        super("Enter query parameters", taskRepository, query);

        try {
            urls = URLFactory.create(taskRepository.getRepositoryUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (query != null) {
            this.titleString = query.getSummary();
            this.queryString = query.getAttribute(RequestTrackerTaskDataHandler.QUERY_ID);
        }
    }


    @Override
    public void createControl(Composite parent) {
        content = new RTQueryPageContent(parent, SWT.NULL);
        init();
        setControl(parent);
    }

    private void init() {
        content.getQueryText().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                queryString = content.getQueryText().getText();
                setPageComplete(isPageComplete());
            }
        });
        content.getQueryText().setText(queryString);

        content.getTitleText().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                titleString = content.getTitleText().getText();
                setPageComplete(isPageComplete());
            }
        });
        content.getTitleText().setText(titleString);
    }

    @Override
    public boolean isPageComplete() {
        return !titleString.isEmpty() && !queryString.isEmpty(); 
    }
    
    @Override
    public String getQueryTitle() {
        return titleString;
    }

    @Override
    public void applyTo(IRepositoryQuery query) {
        query.setAttribute(RequestTrackerTaskDataHandler.QUERY_ID, queryString);
        query.setUrl(urls.getBrowseTicketSearch(queryString));
        query.setSummary(getQueryTitle());
    }

}
