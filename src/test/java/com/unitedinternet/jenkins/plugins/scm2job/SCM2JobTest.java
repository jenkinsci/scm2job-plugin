package com.unitedinternet.jenkins.plugins.scm2job;

import java.util.ArrayList;
import hudson.model.FreeStyleProject;
import hudson.model.Project;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import hudson.scm.SubversionSCM;

import org.jvnet.hudson.test.HudsonTestCase;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests whether git and svn jobs are correctly found
 * and get displayed correctly as text and as links
 * @author kstutz
 */
public class SCM2JobTest extends HudsonTestCase {

    private ArrayList<Project> projects = new ArrayList<Project>();

    private String[] gitProjectNames = {"gitproject1", "gitproject2", "gitproject3"};

    private String[] gitUrls = {"git://github.com/stefanbrausch/scm2job-plugin.git",
                                "git://github.com/abracadabra/hmpf.git",
                                "git://github.com/abracadabra/hmpf.git/", };

    private String[] svnProjectNames = {"svnproject1", "svnproject2", "svnproject3", "svnproject4", "svnproject5"};

    private String[] svnUrls = {"https://svn.foo.org/mojo/trunk/foobar-plugin/",
                                "https://svn.foo.org/mojo/trunk/foobar-plugin",
                                "https://svn.foo.org/mojo/trunk/lorem", 
                                "https://svn.foo.org/mojo/trunk/foobar",
                                "https://svn.foo.org/bar/trunk/foobar"};

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        for (int i = 0; i < gitProjectNames.length; i++) {
            final FreeStyleProject project = createFreeStyleProject(gitProjectNames[i]);
            project.setScm(new GitSCM(gitUrls[i]));
            projects.add(project);
        }
     
        for (int i = 0; i < svnProjectNames.length; i++) {
            final FreeStyleProject project = createFreeStyleProject(svnProjectNames[i]);
            project.setScm(new SubversionSCM(svnUrls[i]));
            projects.add(project);
        }
 
        final FreeStyleProject project = createFreeStyleProject("nullproject");
        project.setScm(new NullSCM());
        projects.add(project);
    }


    /**
     * Tests whether the two projects with the same git repo are found
     * @throws Exception
     */
    public void testGitPlain() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement element = htmlPage.getElementByName("path");
        element.type(gitUrls[1]);
        final HtmlPage results = submit(element.getEnclosingForm(), "Submit");
        
        WebAssert.assertTextPresent(results, gitProjectNames[1]);
        WebAssert.assertTextPresent(results, gitProjectNames[2]);
    }

    /**
     * Tests whether the two projects with the same svn location are found
     * @throws Exception
     */
    public void testSVNPlain() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement element = htmlPage.getElementByName("path");
        element.type(svnUrls[0]);
        final HtmlPage results = submit(element.getEnclosingForm(), "Submit");
        
        WebAssert.assertTextPresent(results, svnProjectNames[0]);
        WebAssert.assertTextPresent(results, svnProjectNames[1]);
        //Test for false positives when scm urls share nearly the same path
        WebAssert.assertTextNotPresent(results, svnProjectNames[3]);
    }
    
    /**
     * Tests whether the links to the two git projects are found
     * @throws Exception
     */
    public void testGitFancy() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement textbox = htmlPage.getElementByName("path");
        textbox.type(gitUrls[1]);
        final HtmlElement radioButton = htmlPage.getElementsByName("format").get(1);
        radioButton.click();
        final HtmlPage results = submit(textbox.getEnclosingForm(), "Submit");
        
        assertStringContains(results.asXml(), "<a href=\"../job/" + gitProjectNames[1] + "/\">");
        assertStringContains(results.asXml(), "<a href=\"../job/" + gitProjectNames[2] + "/\">");
    }
    
    /**
     * Tests whether the links to the two svn projects are found
     * @throws Exception
     */
    public void testSVNFancy() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement textbox = htmlPage.getElementByName("path");
        textbox.type(svnUrls[0]);
        final HtmlElement radioButton = htmlPage.getElementsByName("format").get(1);
        radioButton.click();
        final HtmlPage results = submit(textbox.getEnclosingForm(), "Submit");
        
        assertStringContains(results.asXml(), "<a href=\"../job/" + svnProjectNames[0] + "/\">");
        assertStringContains(results.asXml(), "<a href=\"../job/" + svnProjectNames[1] + "/\">");
    }
    
    /**
     * Tests whether error message is displayed when no path submitted
     * @throws Exception
     */
    public void testNoPathGiven() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");

        final HtmlElement textbox = htmlPage.getElementByName("path");
        final HtmlPage results = submit(textbox.getEnclosingForm(), "Submit");

        WebAssert.assertTextPresent(results, Messages.pathMissing());
    }

    /**
     * Tests whether error message is displayed when no job found
     * @throws Exception
     */
    public void testNoJobFound() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement textbox = htmlPage.getElementByName("path");
        textbox.type("bla");
        final HtmlPage results = submit(textbox.getEnclosingForm(), "Submit");
        
        WebAssert.assertTextPresent(results, "No Jenkins job found");
    }
    
    /**
     * Tests to find all jobs from the same repo
     * @throws Exception
     */
    public void testShowJobsFromSameRepo() throws Exception {
        final WebClient webClient =  new WebClient();
        final HtmlPage htmlPage = webClient.goTo("scm2job");
        
        final HtmlElement textbox = htmlPage.getElementByName("path");
        textbox.type("https://svn.foo.org/mojo");
        final HtmlPage results = submit(textbox.getEnclosingForm(), "Submit");
        
        WebAssert.assertTextNotPresent(results, svnProjectNames[4]);
        WebAssert.assertTextPresent(results, svnProjectNames[0]);
        WebAssert.assertTextPresent(results, svnProjectNames[1]);
        WebAssert.assertTextPresent(results, svnProjectNames[2]);
        WebAssert.assertTextPresent(results, svnProjectNames[3]);

    }
    
    
    /**
     * Tests for false positives when scm urls start the same
     * @throws Exception
     */
/*    public void testFalsePositiveBug() throws Exception {
        final FreeStyleProject project = createFreeStyleProject("BugProject");
        project.setScm(new SubversionSCM("https://svn.foo.org/mojo/trunk/foobar"));
        projects.add(project);
    }
*/    
}
