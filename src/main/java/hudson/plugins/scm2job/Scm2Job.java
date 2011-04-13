package hudson.plugins.scm2job;

import hudson.Plugin;

import hudson.model.AbstractProject;

import hudson.model.Hudson;
import hudson.model.Item;

import hudson.scm.CVSSCM;
import hudson.scm.SCM;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import hudson.plugins.git.GitSCM;
import java.io.IOException;
import java.io.Writer;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.transport.RemoteConfig;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.spearce.jgit.transport.URIish;

/**
 * Entry point of scm2job plugin.
 * 
 * @author Stefan Brausch
 * @plugin
 */
public class Scm2Job extends Plugin {

	public void start() throws Exception {

	}

	public void doGetJobs(StaplerRequest req, StaplerResponse rsp)
			throws IOException {
		Writer writer = rsp.getCompressedWriter(req);
		try {
			String paramPath = req.getParameter("path");
			if (paramPath != null) {
				boolean found = false;

				List<Item> getitems = Hudson.getInstance().getAllItems(
						Item.class);

				for (Item item : getitems) {

					if (checkSCMPath(item, paramPath)) {
						writer.append(item.getName() + "\n");
						found = true;
					}
				}
				if (!found)
					writer.append("No Hudson Job found.");

			} else
				writer.append("Must provide the 'path' parameter.\n");

		} finally {
			writer.close();
		}
	}

	private boolean checkSCMPath(Item item, String path) {
		boolean found = false;

		String[] scmPath = getSCMPath(item);
		if (scmPath != null) {
			for (int i = 0; i < scmPath.length; i++) {
				String checkPath = scmPath[i];

				if (checkPath.length() > 0
						&& checkPath.length() <= path.length()
						&& checkPath.equalsIgnoreCase(path.substring(0,
								checkPath.length()))) {
					found = true;
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

			}
		} else if (scm instanceof CVSSCM) {

			CVSSCM cvs = (CVSSCM) scm;
			String[] locs = cvs.getAllModulesNormalized();
			String cvsRoot = cvs.getCvsRoot();
			scmPath = new String[locs.length];
			for (int i = 0; i < locs.length; i++) {
				String moduleLocation = locs[i];
				scmPath[i] = cvsRoot + "/" + moduleLocation;

			}

		} else if (scm instanceof GitSCM) {

			GitSCM git = (GitSCM) scm;
			List<org.spearce.jgit.transport.RemoteConfig> repoList = git.getRepositories();
			
			scmPath = new String[repoList.size()];
			for (int i = 0; i < repoList.size(); i++) {
				List<URIish> uris = repoList.get(i).getURIs();
				for (Iterator iterator = uris.iterator(); iterator.hasNext();) {
					URIish urIish = (URIish) iterator.next();
					scmPath[i] = urIish.getPath();
					
				}
				

			}
		}
		return scmPath;
	}
}
