# FILE_INDEX

Auto-generated class-to-file lookup for openrndr-geo.
Run `./gradlew fileIndex` to regenerate.

```
src/main/kotlin/App.kt                                                      → AppKt (top-level)  fns: main
src/main/kotlin/TemplateLiveProgram.kt                                      → TemplateLiveProgramKt (top-level)  fns: main
src/main/kotlin/TemplateProgram.kt                                          → TemplateProgramKt (top-level)  fns: main
src/main/kotlin/TestingValidation-phase1.kt                                 → TestingValidation-phase1Kt (top-level)  fns: main
src/main/kotlin/TestingValidation-phase2.kt                                 → TestingValidation-phase2Kt (top-level)  fns: main
src/main/kotlin/geo/Bounds.kt                                               → Bounds
src/main/kotlin/geo/CachedGeoSource.kt                                      → CachedGeoSource
src/main/kotlin/geo/Feature.kt                                              → Feature, ProjectedFeature, ProjectedGeometry, ProjectedLineString, ProjectedMultiLineString, ProjectedMultiPoint, ProjectedMultiPolygon, ProjectedPoint, ProjectedPolygon
src/main/kotlin/geo/GeoJSON.kt                                              → GeoJSON, GeoJSONSource
src/main/kotlin/geo/GeoPackage.kt                                           → GeoPackage, GeoPackageSource
src/main/kotlin/geo/GeoSource.kt                                            → GeoSource
src/main/kotlin/geo/GeoSourceConvenience.kt                                 → GeoSourceConvenienceKt (top-level)  fns: geoSource, geoSourceFromFeatures, geoSourceFromString
src/main/kotlin/geo/GeoStack.kt                                             → GeoStack
src/main/kotlin/geo/Geometry.kt                                             → Geometry, LineString, MultiLineString, MultiPoint, MultiPolygon, Point, Polygon
src/main/kotlin/geo/ProjectionExtensions.kt                                 → ProjectionExtensionsKt (top-level)
src/main/kotlin/geo/SpatialIndex.kt                                         → Quadtree
src/main/kotlin/geo/animation/EasingExtensions.kt                           → EasingExtensionsKt (top-level)  fns: cubicIn, cubicInOut, cubicOut, easeIn, easeInOut, easeOut, linear, none, quadIn, quadInOut, quadOut, quartInOut, sineIn, sineInOut, sineOut
src/main/kotlin/geo/animation/FeatureAnimator.kt                            → FeatureAnimator
src/main/kotlin/geo/animation/GeoAnimator.kt                                → GeoAnimator
src/main/kotlin/geo/animation/ProceduralMotion.kt                           → AnimationWrapper
src/main/kotlin/geo/animation/Tweening.kt                                   → Animation
src/main/kotlin/geo/animation/composition/ChainedAnimation.kt               → ChainedAnimationBuilder
src/main/kotlin/geo/animation/composition/GeoTimeline.kt                    → GeoTimeline
src/main/kotlin/geo/animation/interpolators/HaversineInterpolator.kt        → Position
src/main/kotlin/geo/animation/interpolators/LinearInterpolator.kt           → LinearInterpolatorKt (top-level)  fns: linearInterpolate
src/main/kotlin/geo/animation/package.kt                                    → @JvmName(AnimationPackage)
src/main/kotlin/geo/crs/CRS.kt                                              → CRS
src/main/kotlin/geo/examples/core_CRSTransformTest.kt                       → Core_CRSTransformTestKt (top-level)  fns: main
src/main/kotlin/geo/examples/core_DataLoadingTest.kt                        → Core_DataLoadingTestKt (top-level)  fns: main
src/main/kotlin/geo/examples/core_printSummary.kt                           → Core_printSummaryKt (top-level)  fns: main
src/main/kotlin/geo/examples/layer_BlendModes.kt                            → Layer_BlendModesKt (top-level)  fns: main
src/main/kotlin/geo/examples/layer_Composition.kt                           → Layer_CompositionKt (top-level)  fns: main
src/main/kotlin/geo/examples/layer_Graticule.kt                             → Layer_GraticuleKt (top-level)  fns: main
src/main/kotlin/geo/examples/layer_Output.kt                                → Layer_OutputKt (top-level)  fns: main
src/main/kotlin/geo/examples/proj_HaversineDemo.kt                          → Proj_HaversineDemoKt (top-level)  fns: calculateDistance, drawCityPoint, main
src/main/kotlin/geo/examples/proj_ProjectionTest.kt                         → Proj_ProjectionTestKt (top-level)  fns: main
src/main/kotlin/geo/examples/render_LiveRendering.kt                        → Render_LiveRenderingKt (top-level)  fns: main
src/main/kotlin/geo/exception/ProjectionExceptions.kt                       → AccuracyWarningException, CRSTransformationException, CoordinateSystemException, ProjectionOverflowException, ProjectionUnrepresentableException
src/main/kotlin/geo/internal/OptimizationWarnings.kt                        → OptimizationWarningsKt (top-level)
src/main/kotlin/geo/internal/OptimizedGeoSource.kt                          → OptimizedGeoSourceKt (top-level)
src/main/kotlin/geo/internal/batch/BatchProjectionUtils.kt                  → BatchProjectionUtilsKt (top-level)
src/main/kotlin/geo/internal/batch/CoordinateBatch.kt                       → CoordinateBatchKt (top-level)
src/main/kotlin/geo/internal/cache/CacheKey.kt                              → CacheKeyKt (top-level)
src/main/kotlin/geo/internal/cache/ViewportCache.kt                         → ViewportCacheKt (top-level)
src/main/kotlin/geo/internal/cache/ViewportState.kt                         → ViewportStateKt (top-level)
src/main/kotlin/geo/internal/geometry/OptimizedGeometries.kt                → OptimizedGeometriesKt (top-level)
src/main/kotlin/geo/layer/GeoLayer.kt                                       → GeoLayer
src/main/kotlin/geo/layer/Graticule.kt                                      → GraticuleKt (top-level)  fns: generateGraticule, generateGraticuleSource
src/main/kotlin/geo/layer/package.kt                                        → @JvmName(LayerPackage)
src/main/kotlin/geo/loadGeo.kt                                              → LoadGeoKt (top-level)  fns: loadGeo
src/main/kotlin/geo/project.kt                                              → ProjectKt (top-level)
src/main/kotlin/geo/projection/CRSExtensions.kt                             → CRSExtensionsKt (top-level)
src/main/kotlin/geo/projection/CRSTransformer.kt                            → CRSTransformer
src/main/kotlin/geo/projection/GeoProjection.kt                             → GeoProjection
src/main/kotlin/geo/projection/ProjectionBNG.kt                             → ProjectionBNG
src/main/kotlin/geo/projection/ProjectionConfig.kt                          → FitParameters, ProjectionConfig, ProjectionConfigBuilder
src/main/kotlin/geo/projection/ProjectionEquirectangular.kt                 → ProjectionEquirectangular
src/main/kotlin/geo/projection/ProjectionFactory.kt                         → ProjectionFactory, ProjectionType
src/main/kotlin/geo/projection/ProjectionMercator.kt                        → ProjectionMercator
src/main/kotlin/geo/projection/RawProjection.kt                             → RawProjection
src/main/kotlin/geo/projection/ScreenTransform.kt                           → ScreenTransformKt (top-level)  fns: fromScreen, toScreen
src/main/kotlin/geo/projection/UtilityFunctions.kt                          → UtilityFunctionsKt (top-level)  fns: clampLatitude, isBNGValid, isOnScreen, isValidCoordinate, normalizeCoordinate, normalizeLongitude
src/main/kotlin/geo/projection/internal/ProjectionEquirectangularInternal.kt → ProjectionEquirectangularInternal
src/main/kotlin/geo/projection/internal/ProjectionMercatorInternal.kt       → ProjectionMercatorInternal
src/main/kotlin/geo/projection/package.kt                                   → @JvmName(ProjectionPackage)
src/main/kotlin/geo/render/DrawerGeoExtensions.kt                           → DrawerGeoExtensionsKt (top-level)
src/main/kotlin/geo/render/GeoRenderConfig.kt                               → GeoRenderConfig
src/main/kotlin/geo/render/LineRenderer.kt                                  → LineRendererKt (top-level)  fns: writeLineString
src/main/kotlin/geo/render/MultiRenderer.kt                                 → MultiRendererKt (top-level)  fns: drawMultiLineString, drawMultiPoint, drawMultiPolygon
src/main/kotlin/geo/render/PointRenderer.kt                                 → PointRendererKt (top-level)  fns: drawPoint
src/main/kotlin/geo/render/PolygonRenderer.kt                               → PolygonRendererKt (top-level)  fns: writePolygon, writePolygonWithHoles
src/main/kotlin/geo/render/Shape.kt                                         → Shape
src/main/kotlin/geo/render/Style.kt                                         → Style
src/main/kotlin/geo/render/StyleDefaults.kt                                 → StyleDefaults
src/main/kotlin/geo/render/geometry/AntimeridianSplitter.kt                 → AntimeridianSplitterKt (top-level)
src/main/kotlin/geo/render/geometry/GeometryNormalizer.kt                   → GeometryNormalizerKt (top-level)  fns: normalizeMultiPolygon, normalizePolygon
src/main/kotlin/geo/render/geometry/RingValidator.kt                        → RingValidatorKt (top-level)
src/main/kotlin/geo/render/geometry/WindingNormalizer.kt                    → WindingNormalizerKt (top-level)
src/main/kotlin/geo/render/package.kt                                       → @JvmName(RenderPackage)
src/main/kotlin/geo/render/render.kt                                        → RenderKt (top-level)  fns: drawLineString, drawPolygon
src/main/kotlin/geo/tools/SyntheticData.kt                                  → SyntheticData, SyntheticDataConfig
src/test/kotlin/geo/BoundsTest.kt                                           → BoundsTest
src/test/kotlin/geo/CRSIntegrationTest.kt                                   → CRSIntegrationTest
src/test/kotlin/geo/FeatureTest.kt                                          → FeatureTest
src/test/kotlin/geo/GeoJSONTest.kt                                          → GeoJSONTest
src/test/kotlin/geo/GeoPackageTest.kt                                       → GeoPackageTest
src/test/kotlin/geo/GeoSourceChainingTest.kt                                → GeoSourceChainingTest
src/test/kotlin/geo/GeoSourceSummaryTest.kt                                 → GeoSourceSummaryTest
src/test/kotlin/geo/GeoSourceTest.kt                                        → GeoSourceTest
src/test/kotlin/geo/GeometryTest.kt                                         → GeometryTest
src/test/kotlin/geo/GeometryTransformTest.kt                                → GeometryTransformTest
src/test/kotlin/geo/Phase6IntegrationTest.kt                                → Phase6IntegrationTest
src/test/kotlin/geo/animation/GeoAnimatorTest.kt                            → GeoAnimatorTest
src/test/kotlin/geo/animation/StaggerVerificationTest.kt                    → StaggerVerificationTest
src/test/kotlin/geo/animation/TweeningVerificationTest.kt                   → TweeningVerificationTest
src/test/kotlin/geo/cache/ViewportCacheTest.kt                              → ViewportCacheTest
src/test/kotlin/geo/internal/cache/ViewportCacheTest.kt                     → ViewportCacheTest
src/test/kotlin/geo/performance/BaselineSimulator.kt                        → BaselineSimulator, BenchmarkStats, PerformanceComparison
src/test/kotlin/geo/performance/BaselineSimulatorTest.kt                    → BaselineSimulatorTest
src/test/kotlin/geo/performance/BatchProjectionBenchmark.kt                 → BatchProjectionBenchmark, Benchmark, BenchmarkResult
src/test/kotlin/geo/performance/PerformanceBenchmark.kt                     → BenchmarkScenarioResult, PerformanceBenchmark, ScenarioType
src/test/kotlin/geo/performance/SyntheticDataGenerator.kt                   → SyntheticDataGenerator
src/test/kotlin/geo/performance/SyntheticDataGeneratorTest.kt               → SyntheticDataGeneratorTest
src/test/kotlin/geo/projection/CRSTransformerTest.kt                        → CRSTransformerTest
src/test/kotlin/geo/projection/FitBoundsTest.kt                             → FitBoundsTest
src/test/kotlin/geo/projection/ProjectionTest.kt                            → ProjectionTest
src/test/kotlin/geo/regression/ExampleRegressionTest.kt                     → ExampleRegressionTest
src/test/kotlin/geo/regression/ExampleRunner.kt                             → ExampleResult, ExampleRunner, ExampleRunnerTest
src/test/kotlin/geo/render/DrawerGeoExtensionsTest.kt                       → DrawerGeoExtensionsTest
src/test/kotlin/geo/render/EscapeHatchTest.kt                               → EscapeHatchTest
src/test/kotlin/geo/render/GeometryProjectionTest.kt                        → GeometryProjectionTest
src/test/kotlin/geo/render/LineRendererTest.kt                              → LineRendererTest
src/test/kotlin/geo/render/MultiPolygonRenderingTest.kt                     → MultiPolygonRenderingTest
src/test/kotlin/geo/render/MultiRendererTest.kt                             → MultiRendererTest
src/test/kotlin/geo/render/OptimizedStyleResolutionTest.kt                  → OptimizedStyleResolutionTest
src/test/kotlin/geo/render/PointRendererTest.kt                             → PointRendererTest
src/test/kotlin/geo/render/PolygonRendererTest.kt                           → PolygonRendererTest
src/test/kotlin/geo/render/StyleTest.kt                                     → StyleTest
src/test/kotlin/geo/render/geometry/AntimeridianSplitterTest.kt             → AntimeridianSplitterTest
src/test/kotlin/geo/render/geometry/GeometryNormalizerTest.kt               → GeometryNormalizerTest
src/test/kotlin/geo/render/geometry/OptimizedGeometryNormalizerTest.kt      → OptimizedGeometryNormalizerTest
src/test/kotlin/geo/render/geometry/RingValidatorTest.kt                    → RingValidatorTest
src/test/kotlin/geo/render/geometry/WindingNormalizerTest.kt                → WindingNormalizerTest
examples/anim/01-basic-animation.kt                                         → @JvmName(BasicAnimation)  fns: main
examples/anim/02-geo-animator.kt                                            → @JvmName(GeoAnimator)  fns: main
examples/anim/03-timeline.kt                                                → @JvmName(Timeline)  fns: main
examples/anim/04-stagger-animator.kt                                        → @JvmName(StaggerAnimator)  fns: main
examples/anim/05_chain-animations.kt                                        → @JvmName(ChainAnimations)  fns: main
examples/anim/06-linestring-color-anim.kt                                   → @JvmName(LineStringColorAnim)  fns: main
examples/anim/XX_ripple-demo.kt                                             → @JvmName(RippleDemo)  fns: createRandomFeatures, main
examples/anim/XX_stagger-demo.kt                                            → @JvmName(StaggerDemo)  fns: createFeatureGrid, divider, drawCircle, main
examples/anim/XX_timeline-demo.kt                                           → @JvmName(TimelineDemo)  fns: dividerTimeline, drawCircle, drawSquare, drawTimelineBar, drawTriangle, main
examples/core/01-load-geojson.kt                                            → @JvmName(LoadGeojson)  fns: main
examples/core/02-load-geopackage.kt                                         → @JvmName(LoadGeopackage)  fns: main
examples/core/03-print-summary.kt                                           → @JvmName(PrintSummary)  fns: main
examples/core/04-geostack.kt                                                → @JvmName(GeoStack)  fns: main
examples/core/05-batch-optimization.kt                                      → @JvmName(BatchOptimization)  fns: main
examples/layer/01-graticule.kt                                              → @JvmName(Graticule)  fns: main
examples/layer/02-composition.kt                                            → @JvmName(Composition)  fns: main
examples/proj/01-mercator.kt                                                → @JvmName(Mercator)  fns: main
examples/proj/02-fit-bounds.kt                                              → @JvmName(FitBounds)  fns: main
examples/proj/03-crs-transform.kt                                           → @JvmName(CrsTransform)  fns: main
examples/render/01-points.kt                                                → @JvmName(Points)  fns: main
examples/render/02-linestrings.kt                                           → @JvmName(Linestrings)  fns: main
examples/render/03-polygons.kt                                              → @JvmName(Polygons)  fns: main
examples/render/04-multipolygons.kt                                         → @JvmName(Multipolygons)  fns: main
examples/render/05-style-dsl.kt                                             → @JvmName(StyleDsl)  fns: main
examples/render/06-quick-geo.kt                                             → @JvmName(QuickGeo)  fns: main
examples/render/07-geostack-render.kt                                       → @JvmName(GeoStackRender)  fns: main
examples/render/08-feature-iteration.kt                                     → @JvmName(FeatureIteration)  fns: main
examples/tools/01-synthetic-data-gen.kt                                     → @JvmName(SyntheticDataGen)  fns: main
uat/polygon-bug-fixing.kt                                                   → @JvmName(PolygonBugFixing)  fns: main
uat/viewport-cache-holes.kt                                                 → @JvmName(ViewportCacheHoles)  fns: main
```
