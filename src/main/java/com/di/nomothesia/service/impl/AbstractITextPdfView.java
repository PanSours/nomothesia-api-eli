package com.di.nomothesia.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.AbstractView;

@Service
public abstract class AbstractITextPdfView extends AbstractView {
    
    public AbstractITextPdfView() {
        setContentType("application/pdf");
    }

    @Override
    protected boolean generatesDownloadContent() {
        return true;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        // IE workaround: write into byte array first.
        ByteArrayOutputStream baos = createTemporaryOutputStream();

        // Apply preferences and build metadata.
        Document document = newDocument();
        PdfWriter writer = newWriter(document, baos);
        prepareWriter(model, writer, request);
        buildPdfMetadata(model, document, request);

        // Build PDF document.
        document.open();
        buildPdfDocument(model, document, writer, request, response);
        document.close();

        //Add Page Number
        PdfReader reader = new PdfReader(baos.toByteArray());
        reader.makeRemoteNamedDestinationsLocal();
        baos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, baos);
        BaseFont bf = BaseFont.createFont(getServletContext().getRealPath("./resources/fonts/tahoma.ttf"),
                                          BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        PdfContentByte cb;

        int n = reader.getNumberOfPages();

        for (int i = 1; i <= n; i++) {
            cb = stamper.getOverContent(i);
            float x = cb.getPdfDocument().getPageSize().getWidth() / 2;
            cb.beginText();
            cb.setFontAndSize(bf, 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + i, x, 5, 0);
            cb.endText();
        }

        stamper.close();

        // Flush to HTTP response.
        writeToResponse(response, baos);
    }

    private Document newDocument() {
        return new Document(PageSize.A4);
    }

    private PdfWriter newWriter(Document document, OutputStream os) throws DocumentException {
        return PdfWriter.getInstance(document, os);
    }

    private void prepareWriter(Map<String, Object> model, PdfWriter writer, HttpServletRequest request)
            throws DocumentException {
        writer.setViewerPreferences(getViewerPreferences());
    }

    private int getViewerPreferences() {
        return PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage;
    }

    private void buildPdfMetadata(Map<String, Object> model, Document document, HttpServletRequest request) {
    }

    protected abstract void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception;

}
