package coil3.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter.Companion.DefaultTransform
import coil3.compose.AsyncImagePainter.State
import coil3.compose.internal.AsyncImageState
import coil3.compose.internal.ContentPainterElement
import coil3.compose.internal.UseMinConstraintsMeasurePolicy
import coil3.compose.internal.onStateOf
import coil3.compose.internal.previewHandler
import coil3.compose.internal.requestOfWithSizeResolver
import coil3.compose.internal.transformOf
import coil3.compose.internal.validateRequest
import coil3.request.ImageRequest

/**
 * A composable that executes an [ImageRequest] asynchronously and renders the result.
 *
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param imageLoader The [ImageLoader] that will be used to execute the request.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param placeholder A [Painter] that is displayed while the image is loading.
 * @param error A [Painter] that is displayed when the image request is unsuccessful.
 * @param fallback A [Painter] that is displayed when the request's [ImageRequest.data] is null.
 * @param onLoading Called when the image request begins loading.
 * @param onSuccess Called when the image request completes successfully.
 * @param onError Called when the image request completes unsuccessfully.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param clipToBounds If true, clips the content to its bounds. Else, it will not be clipped.
 * @param keepContentNoneStartOnDraw If true, content never scales and stay in the upper left corner during the drawing phase.
 */
@Composable
@NonRestartableComposable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((State.Loading) -> Unit)? = null,
    onSuccess: ((State.Success) -> Unit)? = null,
    onError: ((State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    keepContentNoneStartOnDraw: Boolean = false,
) = AsyncImage(
    state = AsyncImageState(model, imageLoader),
    contentDescription = contentDescription,
    modifier = modifier,
    transform = transformOf(placeholder, error, fallback),
    onState = onStateOf(onLoading, onSuccess, onError),
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    clipToBounds = clipToBounds,
    keepContentNoneStartOnDraw = keepContentNoneStartOnDraw,
)

/**
 * A composable that executes an [ImageRequest] asynchronously and renders the result.
 *
 * @param model Either an [ImageRequest] or the [ImageRequest.data] value.
 * @param contentDescription Text used by accessibility services to describe what this image
 *  represents. This should always be provided unless this image is used for decorative purposes,
 *  and does not represent a meaningful action that a user can take.
 * @param imageLoader The [ImageLoader] that will be used to execute the request.
 * @param modifier Modifier used to adjust the layout algorithm or draw decoration content.
 * @param transform A callback to transform a new [State] before it's applied to the
 *  [AsyncImagePainter]. Typically this is used to modify the state's [Painter].
 * @param onState Called when the state of this painter changes.
 * @param alignment Optional alignment parameter used to place the [AsyncImagePainter] in the given
 *  bounds defined by the width and height.
 * @param contentScale Optional scale parameter used to determine the aspect ratio scaling to be
 *  used if the bounds are a different size from the intrinsic size of the [AsyncImagePainter].
 * @param alpha Optional opacity to be applied to the [AsyncImagePainter] when it is rendered
 *  onscreen.
 * @param colorFilter Optional [ColorFilter] to apply for the [AsyncImagePainter] when it is
 *  rendered onscreen.
 * @param filterQuality Sampling algorithm applied to a bitmap when it is scaled and drawn into the
 *  destination.
 * @param clipToBounds If true, clips the content to its bounds. Else, it will not be clipped.
 * @param keepContentNoneStartOnDraw If true, content never scales and stay in the upper left corner during the drawing phase.
 */
@Composable
@NonRestartableComposable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    transform: (State) -> State = DefaultTransform,
    onState: ((State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    keepContentNoneStartOnDraw: Boolean = false,
) = AsyncImage(
    state = AsyncImageState(model, imageLoader),
    contentDescription = contentDescription,
    modifier = modifier,
    transform = transform,
    onState = onState,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    clipToBounds = clipToBounds,
    keepContentNoneStartOnDraw = keepContentNoneStartOnDraw,
)

@Composable
private fun AsyncImage(
    state: AsyncImageState,
    contentDescription: String?,
    modifier: Modifier,
    transform: (State) -> State,
    onState: ((State) -> Unit)?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    colorFilter: ColorFilter?,
    filterQuality: FilterQuality,
    clipToBounds: Boolean,
    keepContentNoneStartOnDraw: Boolean,
) {
    val request = requestOfWithSizeResolver(
        model = state.model,
        contentScale = contentScale,
    )
    validateRequest(request)

    Layout(
        modifier = modifier.then(
            ContentPainterElement(
                request = request,
                imageLoader = state.imageLoader,
                modelEqualityDelegate = state.modelEqualityDelegate,
                transform = transform,
                onState = onState,
                contentScale = contentScale,
                filterQuality = filterQuality,
                alignment = alignment,
                alpha = alpha,
                colorFilter = colorFilter,
                clipToBounds = clipToBounds,
                keepContentNoneStartOnDraw = keepContentNoneStartOnDraw,
                previewHandler = previewHandler(),
                contentDescription = contentDescription,
            ),
        ),
        measurePolicy = UseMinConstraintsMeasurePolicy,
    )
}
