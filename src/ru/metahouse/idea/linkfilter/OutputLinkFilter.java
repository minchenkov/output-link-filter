package ru.metahouse.idea.linkfilter;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.ide.DataManager;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.local.CoreLocalFileSystem;
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputLinkFilter
        implements Filter {

    private static final Pattern FILE_PATTERN = Pattern.compile(
            "(/[a-zA-Z0-9/\\-_\\.]+\\.[a-z]+)(:(\\d+))?(:(\\d+))?");

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[-_.!~*\\\\'()a-zA-Z0-9;\\\\/?:\\\\@&=+\\\\$,%#]+)");

    @Override
    public Result applyFilter(String s, int endPoint) {
        int startPoint = endPoint - s.length();
        Matcher matcher = URL_PATTERN.matcher(s);
        if (matcher.find()) {

            return new Result(startPoint + matcher.start(),
                    startPoint + matcher.end(), new OpenUrlHyperlinkInfo(matcher.group(1)));

        } else {
            matcher = FILE_PATTERN.matcher(s);
            if (matcher.find()) {
                Project currentProject = DataKeys.PROJECT.getData(DataManager.getInstance().getDataContext());

                OpenFileDescriptor dd = new OpenFileDescriptor(currentProject,
                        new CoreLocalVirtualFile(new CoreLocalFileSystem(), new File(matcher.group(1))),
                        matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3)) - 1, // line
                        matcher.group(5) == null ? 0 : Integer.parseInt(matcher.group(5)) - 1 // column
                );

                return new Result(startPoint + matcher.start(),
                        startPoint + matcher.end(), new OpenFileHyperlinkInfo(dd));
            } else {
                return new Result(startPoint, endPoint, null, new TextAttributes());
            }
        }
    }
}
