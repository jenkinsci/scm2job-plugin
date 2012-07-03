package com.unitedinternet.jenkins.plugins.scm2job;


import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.AbstractProject;

import hudson.model.Item;
import hudson.plugins.git.GitSCM;

import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.io.Writer;

import java.util.ArrayList;


/**
* Entry point of scm2job plugin.
*
* @author Stefan Brausch
* @plugin
*/

@ExportedBean
@Extension
public class Scm2Job implements RootAction {

    private ArrayList<Job> list = new ArrayList<Job>();
    private Format paramFormat;
    enum Format {TEXT, URL};

    private static final Logger LOGGER = Logger
       .getLogger(Scm2Job.class.getName());

    public final String getIconFileName() {
       return "/plugin/scm2job/icons/scm2job-32x32.png";
    }

    public final String getDisplayName() {
        return "SCM2Job";
    }

    public final String getUrlName() {
        return "/scm2job";
    }

    public Format getFormat(){
        return paramFormat;
    }
    
    public ArrayList<Job> getResults(){
        return list;
    }
    

    public final void doGetJobs(StaplerRequest req, StaplerResponse rsp)
            throws IOException {
        
        list.clear();

        String paramPath = req.getParameter("path");
        String paramString = req.getParameter("format");
                 
        if (paramString.equals("text")){
            paramFormat = Format.TEXT;
        } else {
            paramFormat = Format.URL;
        }
            
        if (paramPath != null && !paramPath.trim().isEmpty()) {

            List<Item> getitems = Hudson.getInstance().getAllItems(
                    Item.class);

            for (Item item : getitems) {
 
                if (checkSCMPath(item, paramPath)) {
                    list.add(new Job(item.getName(), item.getUrl()));
                }
            }

            if (paramFormat.equals(Format.TEXT)){
                rsp.sendRedirect("showResultsPlain");
            } else {
                rsp.sendRedirect("showResultsFancy");
            }
            
        } else {
            Writer writer = rsp.getCompressedWriter(req);
            try{
                writer.append("Must provide the 'path' parameter.\n");              
            } finally{
                writer.close();
            }
        }
    }
    
    
    private boolean checkSCMPath(Item item, String path) {
        Boolean found = false;
        
        String[] scmPath = getSCMPath(item);
        if (scmPath != null) {
            for (int i = 0; i < scmPath.length; i++) {
                String checkPath = scmPath[i];
                LOGGER.fine("check "+path+" against "+scmPath[i]);
                if (checkPath.length() > 0
                        && checkPath.length() <= path.length()
                        && checkPath.equalsIgnoreCase(path.substring(0,
                        checkPath.length()))) {
                    found = true;
                    LOGGER.fine("Job found!");
                }
            }
        }
        
        return found;
    }

    @SuppressWarnings("rawtypes")
    private String[] getSCMPath(Item item) {

        String[] scmPath = null;
        SCM scm = ((AbstractProject<?, ?>) item).getScm();

        if (scm instanceof SubversionSCM) {
            SubversionSCM svn = (SubversionSCM) scm;
            ModuleLocation[] locs = svn.getLocations();
            scmPath = new String[locs.length];
            for (int i = 0; i < locs.length; i++) {
                ModuleLocation moduleLocation = locs[i];
                scmPath[i] = moduleLocation.remote;
                LOGGER.fine(scmPath[i]+" added");
            }
        } 
        
       else if (scm instanceof GitSCM) {

            final GitSCM git = (GitSCM) scm;
            final List<RemoteConfig> repoList = git.getRepositories();

            scmPath = new String[repoList.size()];
            for (int i = 0; i < repoList.size(); i++) {
                List<URIish> uris = repoList.get(i).getURIs();
                for (Iterator iterator = uris.iterator(); iterator.hasNext();) {
                    URIish urIish = (URIish) iterator.next();
                    scmPath[i] = urIish.toString();
                    LOGGER.fine(scmPath[i]+" added");
                }
            }
        }

        return scmPath;
    }
       
    public class Job {
        String name;
        String url;
        
        Job(String name, String url){
            this.name = name;
            this.url = url;
        }
        
        public String getName(){
            return name;
        }
        
        public String getUrl(){
            return url;
        }
    }
}
