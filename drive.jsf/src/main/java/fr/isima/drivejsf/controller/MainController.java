package fr.isima.drivejsf.controller;

import java.io.*;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import fr.isima.drivejsf.ejb.ServiceEJB;
import fr.isima.drivejsf.entity.Data;
import fr.isima.drivejsf.entity.Document;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean
@SessionScoped
public class MainController implements Serializable {

	private static final long serialVersionUID = 3973801993975443027L;

    private List<Document> rootDocuments;
    private Document selectedDocument = null;
    private StreamedContent downloadableDocument = null;

	@EJB
	private ServiceEJB service;
	
	public void doLogin() {
        //System.out.println("isRoot : " + service.isRoot ("1"));
        //System.out.println("isFolder : " + service.isFolder ("1"));
        //System.out.println("getList : " + service.getList ("1", null));
        //System.out.println("getList : " + service.getList ("1", "1"));
        //System.out.println("getDocument : " + service.getDocument ("1"));
        //System.out.println("getDocumentForUri : " + service.getDocumentForUri ("2", "aleanar_folder"));
    }

    @PostConstruct
    private void postConstruct() {
        rootDocuments = service.getList ("1", null);
    }

    public List<Document> getRootDocuments() {
        return rootDocuments;
    }

    public void setRootDocuments(List<Document> rootDocuments) {
        this.rootDocuments = rootDocuments;
    }

    public Document getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(Document selectedDocument) {
        this.selectedDocument = selectedDocument;
    }

    public StreamedContent getDownloadableDocument() {
        Document current = selectedDocument;
        Data data;
        InputStream stream;

        if (current != null) {
            data = current.getDataid();
            stream = new ByteArrayInputStream(data.getData());
            downloadableDocument = new DefaultStreamedContent(stream, "text/plain", current.getName());
        } else {
            downloadableDocument = null;
        }
        System.out.println(downloadableDocument);

        return downloadableDocument;
    }

    public void setDownloadableDocument(StreamedContent downloadableDocument) {
        this.downloadableDocument = downloadableDocument;
    }

}