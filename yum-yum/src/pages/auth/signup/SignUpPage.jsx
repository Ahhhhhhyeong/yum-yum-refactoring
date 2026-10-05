import React, { useState } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
// 훅
import useAuth from '@/hooks/useAuth';
// 컴포넌트
import Header from '@/components/Header';
import SignupAgreements from './pages/SignupAgreements';
import SignupStep1 from './pages/SignupStep1';
import SignupStep2 from './pages/SignupStep2';
import SignupStep3 from './pages/SignupStep3';

export default function SignUpPage() {
  const methods = useForm({
    defaultValues: {
      // step 0
      agreeAll: false,
      service: false,
      privacy: false,
      sensitive: false,
      // step 1
      name: '',
      email: '',
      pw: '',
      pwCheck: '',
      // step 2
      gender: '',
      // age: '', age 대신 birthYear로 수정
      birthYear: '',
      height: '',
      weight: '',
      // step 3
      goals: '',
      targetWeight: '',
      targetExercise: '',
    },
    mode: 'onBlur',
  });
  const { signUp } = useAuth();
  const navigate = useNavigate();
  const [signUpStep, setSignUpStep] = useState(1);

  const onSubmit = async (data) => {
    try {
      const result = await signUp(data);
      toast.success(result.message);
      navigate('/login', { replace: true });
    } catch (error) {
      toast.error(error.message || '회원가입 요청 전송 실패');
    }
  };

  return (
    <div>
      <Header />
      {signUpStep > 1 && (
        <div className='flex items-center justify-between px-[20px] py-[40px]'>
          <h3 className='text-2xl font-bold'>회원가입</h3>
          <div className='w-[60px] py-1 bg-gray-200 font-bold text-center rounded-full'>
            {signUpStep - 1} / 3
          </div>
        </div>
      )}

      <FormProvider {...methods}>
        <form onSubmit={methods.handleSubmit(onSubmit)}>
          {signUpStep === 1 && <SignupAgreements onNext={() => setSignUpStep(2)} />}
          {signUpStep === 2 && (
            <SignupStep1 onPrev={() => setSignUpStep(1)} onNext={() => setSignUpStep(3)} />
          )}
          {signUpStep === 3 && (
            <SignupStep2 onPrev={() => setSignUpStep(2)} onNext={() => setSignUpStep(4)} />
          )}
          {signUpStep === 4 && <SignupStep3 onPrev={() => setSignUpStep(3)} />}
        </form>
      </FormProvider>
    </div>
  );
}
