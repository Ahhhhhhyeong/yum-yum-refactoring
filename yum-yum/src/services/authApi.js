// 개발 서버의 /api 프록시를 통해 Spring 백엔드로 전달합니다.
export async function submitSignup(user) {
  const { pwCheck, agreeAll, ...request } = user;
  void pwCheck;
  void agreeAll;

  const response = await fetch('/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new Error(`회원가입 요청 전송에 실패했습니다. (HTTP ${response.status})`);
  }

  return response.json();
}

export async function authenticatedRequest(path, options = {}) {
  // 로그인 API와 같은 Firebase 프로젝트의 ID 토큰을 사용합니다.
  const { auth } = await import('./firebase');
  await auth.authStateReady();
  if (!auth.currentUser) {
    throw new Error('로그인이 필요합니다.');
  }

  // SDK가 만료된 토큰을 갱신합니다. 토큰은 별도로 localStorage에 저장하지 않습니다.
  const idToken = await auth.currentUser.getIdToken();
  const headers = new Headers(options.headers);
  headers.set('Authorization', `Bearer ${idToken}`);
  const response = await fetch(path, { ...options, headers });

  if (response.status === 401) {
    throw new Error('백엔드 인증에 실패했습니다. 다시 로그인해주세요.');
  }
  if (response.status === 403) {
    throw new Error('이 요청에 대한 접근 권한이 없습니다.');
  }
  if (!response.ok) {
    throw new Error(`백엔드 요청에 실패했습니다. (HTTP ${response.status})`);
  }
  return response;
}

export async function getCurrentUser() {
  const response = await authenticatedRequest('/api/users/me');
  return response.json();
}

export async function signOutFirebase() {
  const [{ auth }, { signOut }] = await Promise.all([import('./firebase'), import('firebase/auth')]);
  await signOut(auth);
}
