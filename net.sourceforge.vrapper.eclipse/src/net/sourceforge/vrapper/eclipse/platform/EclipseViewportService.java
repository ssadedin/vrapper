package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;

public class EclipseViewportService implements ViewportService {

    private final ITextViewer textViewer;
    private final ITextViewerExtension5 textViewer5;

    public EclipseViewportService(ITextViewer textViewer) {
        this.textViewer = textViewer;
        this.textViewer5 = textViewer instanceof ITextViewerExtension5
                         ? (ITextViewerExtension5) textViewer : null;
    }

    public void setRepaint(boolean redraw) {
        textViewer.getTextWidget().setRedraw(redraw);
    }

    public void exposeModelPosition(Position position) {
        if (textViewer5 != null) {
            textViewer5.exposeModelRange(new Region(position.getModelOffset(), 1));
        }
    }

}
