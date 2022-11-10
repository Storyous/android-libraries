package com.storyous.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression


class ToastDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = listOf("makeText")

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        super.visitMethodCall(context, node, method)
        if (context.evaluator.isMemberInClass(method, "android.widget.Toast")) {
            reportUsage(context, node)
        }
    }

    private fun reportUsage(context: JavaContext, node: UCallExpression) {
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getCallLocation(
                call = node,
                includeReceiver = true,
                includeArguments = true
            ),
            message = "Use Toaster instead of direct Toast"
        )
    }

    companion object {
        private val IMPLEMENTATION =
            Implementation(ToastDetector::class.java, Scope.JAVA_FILE_SCOPE)

        val ISSUE: Issue = Issue
            .create(
                id = "ToastDetector",
                briefDescription = "Use Toaster instead of direct Toast",
                explanation = """
                On Android 7.1 Toast can cause crash with BadTokenException. Toaster handle this
                by using ToastCompat wrapper which catch that exception. 
            """.trimIndent(),
                category = Category.CORRECTNESS,
                priority = 9,
                severity = Severity.ERROR,
                androidSpecific = true,
                implementation = IMPLEMENTATION
            )
    }
}
