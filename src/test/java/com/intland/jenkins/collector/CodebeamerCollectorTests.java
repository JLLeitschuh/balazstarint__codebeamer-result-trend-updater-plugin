/*
 * Copyright (c) 2015 Intland Software (support@intland.com)
 */
package com.intland.jenkins.collector;

import com.intland.jenkins.api.CodebeamerApiClient;
import com.intland.jenkins.collector.dto.CodebeamerDto;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, Project.class})
public class CodebeamerCollectorTests {
    @Mock CodebeamerApiClient apiClient;
    @Mock ItemGroup<Item> projectParent;
    @Mock PluginWrapper pluginWrapper;
    @Mock AbstractProject project;
    @Mock BuildListener listener;
    @Mock AbstractBuild build;
    @Mock PrintStream logger;
    @Mock Jenkins jenkins;
    @Mock Plugin plugin;
    @Mock Node builtOn;
    @Mock User user;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMarkupCreationWithoutPlugins() throws Exception {
        mockJenkins();

        String expectedMarkup = "[{JenkinsBuildTrends}]\n" +
                "//DO NOT MODIFY! \n//Generated by plugin version: 10.x.x at: 1970-01-01 01:01:15\n" +
                "!2 %%(color: #000000;)Build #0 (1970-01-01 01:01:15)%!\n" +
                "[{Table\n\n|__Duration__\n|[1 min, 15 sec|http://localhost:8080/jenkins/myproject/01/buildTimeTrend] @ Jenkins\n\n" +
                "| \n__Test Result__ \n|__[0/0|http://localhost:8080/jenkins/myproject/01/testReport/] failures__ \n\n" +
                "|__[Tested changes|http://localhost:8080/jenkins/myproject/01/changes]__\n|__\n" +
                "* [#1000|ISSUE:1000] commit message (admin)\n" +
                "* [#1000|ISSUE:1000] commit message (admin)\n \n\n" +
                "|__Repository__\n|Unsupported SCM\n}] \n";

        long currentTime = 75000l;
        CodebeamerDto codebeamerDto = CodebeamerCollector.collectCodebeamerData(build, listener, apiClient, currentTime);
        Assert.assertEquals(expectedMarkup, codebeamerDto.getMarkup());
    }

    private void mockJenkins() {
        mockStatic(Jenkins.class);

        when(Jenkins.getInstance()).thenReturn(jenkins);
        when(build.getProject()).thenReturn(project);
        when(project.getParent()).thenReturn(projectParent);
        when(jenkins.getPlugin(anyString())).thenReturn(plugin);
        when(plugin.getWrapper()).thenReturn(pluginWrapper);
        when(build.getBuiltOn()).thenReturn(builtOn);
        when(listener.getLogger()).thenReturn(logger);

        when(user.toString()).thenReturn("admin");
        when(builtOn.getDisplayName()).thenReturn("Jenkins");
        when(project.getUrl()).thenReturn("myproject/");
        when(project.getShortUrl()).thenReturn("01/");
        when(pluginWrapper.getVersion()).thenReturn("10.x.x");
        when(jenkins.getRootUrl()).thenReturn("http://localhost:8080/jenkins/");
        when(build.getUrl()).thenReturn("myproject/01/");
        DummyEntry entry = new DummyEntry("#1000 commit message", user);
        ChangeLogSet changeLogSet = new DummyChangelog(entry, entry);
        when(build.getChangeSet()).thenReturn(changeLogSet);
    }
}

class DummyChangelog extends ChangeLogSet {
    private Entry[] entries;
    private int i = 0;

    public DummyChangelog(Entry... entries) {
        super(null, null);
        this.entries = entries;
    }

    @Override
    public boolean isEmptySet() {
        return false;
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            @Override
            public boolean hasNext() {
                return i < entries.length ;
            }

            @Override
            public Object next() {
                return entries[i++];
            }
        };
    }
}

class DummyEntry extends Entry {
    private String message;
    private User user;

    public DummyEntry(String message, User user) {
        this.message = message;
        this.user = user;
    }

    @Override
    public String getMsg() {
        return message;
    }

    @Override
    public User getAuthor() {
        return user;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        return null;
    }
}