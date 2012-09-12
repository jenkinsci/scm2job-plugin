
package hudson.plugins.scm2job;

import java.util.Iterator;
import java.util.List;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Project;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;
import hudson.tasks.Shell;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.HudsonTestCase.WebClient;
import org.jvnet.hudson.test.recipes.LocalData;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;



public class SCM2JobTest extends HudsonTestCase {

//funktioniert  
/*    public void test1() throws Exception {
        FreeStyleProject project = createFreeStyleProject();
        project.getBuildersList().add(new Shell("echo hello"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        String s = FileUtils.readFileToString(build.getLogFile());
        System.out.println("HELLO!");
        assertTrue(s.contains("+ echo hello"));
      }
*/    

    public void testSVN() throws Exception {

        String scmPath = "https://svn.codehaus.org/mojo/trunk/sandbox/ckjm-maven-plugin";
        String projectName = "svnproject";
        
        FreeStyleProject project = createFreeStyleProject(projectName);
        project.setScm(new SubversionSCM(scmPath));
        
        final WebClient webClient =  new WebClient();
        HtmlPage htmlpage = webClient.goTo("scm2job");
        
        HtmlElement element = htmlpage.getElementByName("path");
        element.type(scmPath);
        HtmlPage results = submit(element.getEnclosingForm(), "submit");
        
        System.out.println(results.asXml());
       
        assertTrue(results.asXml().contains(projectName));
    }
        

    public void testGit() throws Exception {

        String scmPath = "git://github.com/stefanbrausch/scm2job-plugin.git";
        String projectName = "gitproject";
        
        FreeStyleProject project = createFreeStyleProject(projectName);
        project.setScm(new SubversionSCM(scmPath));
        
        final WebClient webClient =  new WebClient();
        HtmlPage htmlpage = webClient.goTo("scm2job");
        
        HtmlElement element = htmlpage.getElementByName("path");
        element.type(scmPath);
        HtmlPage results = submit(element.getEnclosingForm(), "submit");
        
        System.out.println(results.asXml());
       
        assertTrue(results.asXml().contains(projectName));
    }
}
