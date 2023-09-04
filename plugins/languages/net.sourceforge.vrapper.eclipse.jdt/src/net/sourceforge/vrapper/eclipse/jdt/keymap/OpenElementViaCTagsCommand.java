package net.sourceforge.vrapper.eclipse.jdt.keymap;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.CTags;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Equivalent of "goto decl" but using CTags
 */
public class OpenElementViaCTagsCommand extends EclipseCommand {

		public OpenElementViaCTagsCommand(String action) {
			super(action);
		}

		public void execute(final EditorAdaptor editorAdaptor) throws CommandExecutionException {

			final String symbol = VimUtils.getWordUnderCursor(editorAdaptor, false);
		    
		    try {
				
				IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        IEditorPart editorPart = activeWindow.getActivePage().getActiveEditor();

		    	if (editorPart != null) {
		    		IEditorInput input = editorPart.getEditorInput();
		    		IFile editorFile = (IFile) input.getAdapter(IFile.class);
		    		if(editorFile == null) {
						VrapperLog.info("CTags navigation unable to resolve current editor file");
		    			return;
		    		}
		    			
					IProject project = editorFile.getProject();
					IFile ctagsFile = project.getFile("tags");

					CTags ctags = CTags.parse(ctagsFile.getLocation().toFile());
					CTags.CTag tag = ctags.index.get(symbol);
					if(tag == null) {
						VrapperLog.info("Symbol " + symbol + " not found in tag file " + ctagsFile);
						return;
					}

					VrapperLog.info("Found symbol " + symbol + " in tag file " + ctagsFile + " with tag definition: " + tag);

					editorAdaptor.getFileService().openFile(tag.file);
				
					if(tag.address.startsWith("/")) {
						final String searchValue = tag.getCleanAddress();
						
						EditorAdaptor ea = findEditor(tag.file);
						if(ea != null) {
							searchTextInEditor(ea, searchValue);
						}
						else {
							VrapperLog.info("No editor found matching path " + tag.file);
						}
					}
		    	}					
		    } catch (IOException e) {
		        VrapperLog.error("Error attempting CTags navigation", e);
			}
		}

		void searchTextInEditor(EditorAdaptor ea, final String searchValue) {
			final Search search = new Search(searchValue, false, true, new SearchOffset.Begin(0), true);

			Position startPosition = ea.getPosition().addModelOffset(-ea.getPosition().getModelOffset());

			VrapperLog.info("Highlighting search pattern: " + searchValue);
			SearchAndReplaceService searchAndReplaceService = ea.getSearchAndReplaceService();
			SearchResult result = searchAndReplaceService.find(search, startPosition);
			if(result.isFound()) {
				ea.setPosition(result.getStart(), StickyColumnPolicy.NEVER);
				VrapperLog.info("Search result is: " + result);
			}
			else {
				VrapperLog.info("CTags search text " + searchValue + " not found");
			}

		}
		
		/**
		 * Search for an editor matching the given file path
		 * 
		 * @param filePath	File path to search for
		 * @return	editor matching that file path or null if none found
		 */
		EditorAdaptor findEditor(String filePath) {
			for(Map.Entry<IWorkbenchPart, InputInterceptor> e : InputInterceptorManager.INSTANCE.getInterceptors().entrySet()) {

				EditorPart ep = (EditorPart) e.getKey();
				EditorAdaptor ea = e.getValue().getEditorAdaptor();

				IEditorInput inp = ep.getEditorInput();
				if(inp instanceof FileEditorInput) {
					String path = ((FileEditorInput) inp).getFile().getProjectRelativePath().toFile().getPath();
					VrapperLog.info(" editor path is: " + path);

					if(path.equals(filePath)) {
						return ea;
					}
				}
			}
			
			return null;
		}
	}