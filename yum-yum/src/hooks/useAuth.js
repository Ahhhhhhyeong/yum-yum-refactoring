// 유저 로그인 & 회원가입 상태 확인 훅
import { useCallback } from 'react';
import toast from 'react-hot-toast';
import { checkUserEmail, loginUser } from '../services/userApi';
import { getCurrentUser, signOutFirebase, submitSignup } from '../services/authApi';
import { useUserStore } from '../stores/useUserStore';

export default function useAuth() {
  const {
    isAuthenticated, // default: false
    setLoading,
    clearError,
    loginSuccess, // 로그인 성공 시 (set)
    loginFailure, // 로그인 실패 시 (set)
    logout: logoutStore, // 로그아웃 시(set)
    checkEmail,
    checkResult, // 이메일 체크한 결과 값(set)
    signupFailure,
  } = useUserStore();

  // 로그인
  const login = useCallback(
    async (userId, password) => {
      setLoading(true);
      clearError();

      try {
        const result = await loginUser({ userid: userId, password });
        if (!result.success) {
          throw new Error(result.error);
        }
        // Firebase 로그인뿐 아니라 Spring Security의 토큰 인증도 성공해야 로그인 처리합니다.
        const currentUser = await getCurrentUser();
        loginSuccess(currentUser.uid);
        toast.success('로그인 성공!');
        return { success: true };
      } catch (error) {
        await signOutFirebase().catch(() => undefined);
        loginFailure(error.message);
        toast.error(error.message);
        return { success: false, error: error.message };
      }
    },
    [setLoading, clearError, loginSuccess, loginFailure],
  );

  const logout = useCallback(async () => {
    try {
      await signOutFirebase();
      logoutStore();
    } catch {
      toast.error('로그아웃에 실패했습니다. 다시 시도해주세요.');
    }
  }, [logoutStore]);

  // 이메일 중복확인
  const useCheckEmail = useCallback(async (userId) => {
    setLoading(true);
    clearError();
    const result = await checkUserEmail({ userId });
    // console.log(result);
    return {
      result: result,
      message: result ? '중복된 이메일입니다.' : '사용가능한 이메일입니다.',
    };
  });

  // 회원가입
  const signUp = useCallback(async (user) => {
    setLoading(true);
    clearError();

    try {
      return await submitSignup(user);
    } catch (error) {
      signupFailure({ error: error.message });
      throw error;
    } finally {
      setLoading(false);
    }
  }, [setLoading, clearError, signupFailure]);

  return {
    isAuthenticated,
    login,
    logout,
    useCheckEmail,
    signUp,
    checkResult,
    checkEmail,
  };
}
