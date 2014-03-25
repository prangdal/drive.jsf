package fr.isima.drivejsf.controller;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import fr.isima.drivejsf.ejb.DocumentServiceEJB;
import fr.isima.drivejsf.ejb.ShareServiceEJB;
import fr.isima.drivejsf.ejb.UserServiceEJB;
import fr.isima.drivejsf.entity.Data;
import fr.isima.drivejsf.entity.Document;
import fr.isima.drivejsf.exception.NoDataFoundException;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import fr.isima.drivejsf.entity.User;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean
@SessionScoped
public class MainController implements Serializable {

	private static final long serialVersionUID = 3973801993975443027L;

    private List<Document> rootDocuments;
    private Document selectedDocument = null;
    private StreamedContent downloadableDocument = null;
    private String currentUserId = "1";
    private Document currentDocument = null;
    private List<String> users;
    private String shareUser;
    private String folderName = "";
    private String currentPath = "";
    private String searchInput = "";

    @EJB
    private DocumentServiceEJB documentService;

    @EJB
    private UserServiceEJB userService;

    @EJB
    private ShareServiceEJB shareService;

    @PostConstruct
    private void postConstruct() {
        rootDocuments = documentService.getList(currentUserId, null);
        users = userService.getLoginList();
    }

    private void setFileStreamedContent(Document document) {
        Data data;
        InputStream stream;

        data = document.getDataid();
        stream = new ByteArrayInputStream(data.getData());
        downloadableDocument = new DefaultStreamedContent(stream, "text/plain", document.getName());
    }

    private void zipFolder (Document folder, ZipOutputStream zos, String root) throws IOException {
        List<Document> children = documentService.getList(currentUserId, folder.getId().toString());

        for (Document child : children) {
            if (documentService.isFolder(child.getId().toString())) {
                zipFolder(child, zos, root + "/" + child.getName());
            } else {
                ZipEntry entry = new ZipEntry(root + "/" + child.getName());
                Data data = child.getDataid();

                entry.setSize(data.getData().length);
                zos.putNextEntry(entry);
                zos.write(data.getData());
                zos.closeEntry();
            }
        }
    }

    private void setFolderStreamContent(Document folder) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        InputStream stream;

        try {
            zipFolder(folder, zos, "/" + folder.getName());
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stream = new ByteArrayInputStream(baos.toByteArray());

        downloadableDocument = new DefaultStreamedContent(stream, "text/plain", folder.getName() + ".zip");
    }

    private void updateRootDocuments() {
        Document current = currentDocument;

        if (current != null) {
            try {
                rootDocuments = documentService.getList(currentUserId, current.getId().toString());
                currentPath = current.getUri();
            } catch (NoDataFoundException noDataException) {
                System.out.println("The document " + current.getName() + " is not a folder.");
            }
        } else {
            rootDocuments = documentService.getList(currentUserId, null);
            currentPath = "";

        }
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public String getSearchInput() {
        return searchInput;
    }

    public void setSearchInput(String searchInput) {
        this.searchInput = searchInput;
    }

    public StreamedContent getDownloadableDocument() {
        Document current = selectedDocument;

        if (current != null) {
            if (documentService.isFolder(current.getId().toString())) {
                setFolderStreamContent(current);
            } else {
                setFileStreamedContent(current);
            }

        } else {
            downloadableDocument = null;
        }

        return downloadableDocument;
    }

    public void setDownloadableDocument(StreamedContent downloadableDocument) {
        this.downloadableDocument = downloadableDocument;
    }

    public void onDocumentDblClck() {
        if (selectedDocument != null) {
            if (documentService.isFolder(selectedDocument.getId().toString())) {
                currentDocument = selectedDocument;
                updateRootDocuments();
            }
        }
    }

    public void onDocumentSelect() {

    }

    public void onReturnToParent() {
        if (rootDocuments != null && currentDocument != null) {
            currentDocument = currentDocument.getParentid();

            updateRootDocuments();
        }
    }

    public void onDeleteDocument() {
        Document current = selectedDocument;

        if (current != null) {
            documentService.deleteDocument(current.getId().toString());

            updateRootDocuments();
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        documentService.addDocument(event.getFile(), currentDocument, currentUserId);
        updateRootDocuments();
    }

    public void shareDocument () {
        System.out.println("share doc : " + shareUser + " " + selectedDocument);
        shareService.shareDocument(shareUser, selectedDocument.getId());
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getUsers () {
        return users;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public String getShareUser () {
        return shareUser;
    }

    public void onAddFolder() {
        documentService.addFolder(folderName, currentDocument, currentUserId);

        updateRootDocuments();
    }

    public void onSearch() {
        currentPath = "Search results";
        currentDocument = null;
        rootDocuments = documentService.searchDocuments(searchInput, currentUserId);
    }

    public void onReset() {
        selectedDocument = null;
        downloadableDocument = null;
        currentDocument = null;
        folderName = "";
        currentPath = "";
        searchInput = "";

        postConstruct();
    }
}