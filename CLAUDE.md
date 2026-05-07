# CLAUDE.md (tap-target-compose)

이 파일은 `tap-target-compose` 라이브러리를 Compose Multiplatform(CMP)으로 전환하고 유지보수하기 위한 가이드입니다.

## 프로젝트 개요

- **목적**: Jetpack Compose 및 Compose Multiplatform을 위한 TapTarget(Feature Discovery) UI 라이브러리
- **플랫폼**: Android, iOS (추후 Desktop, Web 확장 고려)
- **언어**: Kotlin (공통 Logic 및 UI)
- **빌드 시스템**: Gradle (Kotlin DSL)

## 핵심 목표: CMP 전환 가이드

1.  **Android 의존성 제거**: `commonMain`에서 `android.*` 패키지 의존성을 제거합니다. `Context`, `Canvas` (Android 전용), `Paint` (Android 전용) 등을 CMP 공통 API로 대체합니다.
2.  **expect/actual 활용**: 플랫폼별 특화 기능(예: Haptic Feedback, 특정 가속도계 등)이 필요한 경우에만 제한적으로 사용합니다.
3.  **Canvas API**: TapTarget의 핵심인 원형 하이라이트와 텍스트 배치는 플랫폼 공통 `androidx.compose.ui.graphics.Canvas`를 사용합니다.
4.  **애니메이션**: `androidx.compose.animation.core`를 활용하여 모든 플랫폼에서 동일한 사용자 경험을 제공합니다.

## 프로젝트 구조 (전환 목표)

```text
tap-target-compose/
├── src/
│   ├── commonMain/kotlin/     # 공통 UI 로직 및 Composable
│   │   └── com/psoffritti/taptargetcompose/
│   │       ├── TapTarget.kt       # 메인 Composable 함수
│   │       ├── TapTargetStyle.kt  # 스타일 정의
│   │       └── internal/          # 수학 계산 및 렌더링 로직
│   ├── androidMain/kotlin/    # Android 전용 구현 및 리소스
│   └── iosMain/kotlin/        # iOS 전용 구현
sample-app/                    # CMP 샘플 앱 (Android/iOS 대응)
```

## 코딩 컨벤션

- **Compose First**: 모든 UI는 Compose Multiplatform API로만 작성합니다.
- **Modifier 사용**: 모든 public Composable은 `modifier: Modifier = Modifier` 파라미터를 제공해야 합니다.
- **불변성**: UI 상태(State)는 불변 객체로 관리하며, `remember`와 `mutableStateOf`를 적절히 사용합니다.
- **KDoc 작성**: 공개 인터페이스 및 함수에는 반드시 KDoc 주석을 추가합니다.

## 주요 명령어

- `./gradlew build`: 전체 프로젝트 빌드
- `./gradlew :tap-target-compose:assemble`: 라이브러리 빌드
- `./gradlew :sample-app:assembleDebug`: Android 샘플 실행 파일 생성
- `./gradlew publishToMavenLocal`: 로컬 Maven 저장소에 배포

## AI 작업 지침

- **코드 제안**: 항상 `commonMain`에 코드를 작성하는 것을 기본으로 합니다.
- **의존성 확인**: `androidx.compose.*` 의존성은 사용 가능하나, `androidx.activity.*`나 `androidx.appcompat.*` 등은 `commonMain`에서 피해야 합니다.
- **리소스**: 문자열이나 아이콘은 CMP의 `compose.components.resources` 라이브러리를 사용하도록 제안하세요.
