/**
 * Preview bridge for the static Spring Boot deliverable.
 * Design: The actual interface lives under public/src/main/resources/static to preserve the requested no-build output.
 */
export default function Home() {
  return (
    <iframe
      title="키라키라 성지순례 정적 프론트엔드 미리보기"
      src="/src/main/resources/static/index.html"
      className="h-screen w-full border-0"
    />
  );
}
