import React from 'react';
import { Controller, useFormContext } from 'react-hook-form';
// 컴포넌트
import BasicButton from '@/components/button/BasicButton';
import Input from '@/components/common/Input';

export default function SignupStep1({ onPrev, onNext }) {
  const { control, handleSubmit, watch } = useFormContext();
  const pw = watch('pw');
  return (
    <>
      <div className='flex flex-col gap-[28px] px-[20px] min-h-[calc(100vh-220px)]'>
        {/* 이름 */}
        <Controller
          name='name'
          control={control}
          rules={{
            required: '이름을 입력해주세요',
            minLength: { value: 2, message: '2자 이상 입력해주세요.' },
            maxLength: { value: 5, message: '5자 이하로 입력해주세요.' },
            pattern: { value: /^[가-힣]+$/, message: '이름을 정확하게 입력해주세요.' },
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='name' className='text-sm font-bold text-gray-500'>
                이름 <strong className='text-secondary font-extrabold'>*</strong>
              </label>
              <Input
                {...field}
                id='name'
                placeholder='이름'
                status={fieldState.error ? 'error' : 'default'}
                errorMessage={fieldState.error?.message}
                maxLength='5'
              />
            </div>
          )}
        />

        {/* 연결 테스트 단계에서는 이메일 중복확인을 생략합니다. */}
        <Controller
          name='email'
          control={control}
          rules={{
            required: '이메일을 입력해주세요',
            pattern: {
              value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
              message: '올바른 이메일 형식이 아니에요.',
            },
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='email' className='text-sm font-bold text-gray-500'>
                이메일 <strong className='text-secondary font-extrabold'>*</strong>
              </label>
              <div className='flex gap-[12px]'>
                <Input
                  {...field}
                  id='email'
                  placeholder='yumyum@gmail.com'
                  status={fieldState.error ? 'error' : 'default'}
                  errorMessage={fieldState.error?.message}
                  className='flex-1'
                />
              </div>
            </div>
          )}
        />

        {/* 비밀번호 */}
        <Controller
          name='pw'
          control={control}
          rules={{
            required: '비밀번호를 입력해주세요',
            minLength: { value: 8, message: '8자 이상 입력해주세요.' },
            maxLength: { value: 20, message: '20자 이하로 입력해주세요.' },
            pattern: {
              value: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,20}$/,
              message: '영문, 숫자, 특수문자를 모두 포함해야 해요.',
            },
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='pw' className='text-sm font-bold text-gray-500'>
                비밀번호 <strong className='text-secondary font-extrabold'>*</strong>
              </label>
              <Input
                {...field}
                id='pw'
                type='password'
                placeholder='영문, 숫자, 특수문자 포함 8~20자'
                status={fieldState.error ? 'error' : 'default'}
                errorMessage={fieldState.error?.message}
                maxLength='20'
              />
            </div>
          )}
        />

        {/* 비밀번호 확인 */}
        <Controller
          name='pwCheck'
          control={control}
          rules={{
            required: '비밀번호 확인을 입력해주세요',
            validate: (value) => value === pw || '비밀번호가 일치하지 않아요.',
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='pwCheck' className='text-sm font-bold text-gray-500'>
                비밀번호 확인 <strong className='text-secondary font-extrabold'>*</strong>
              </label>
              <Input
                {...field}
                id='pwCheck'
                type='password'
                placeholder='영문, 숫자, 특수문자 포함 8~20자'
                status={fieldState.error ? 'error' : 'default'}
                errorMessage={fieldState.error?.message}
                maxLength='20'
              />
            </div>
          )}
        />
      </div>

      <div className='sticky bottom-0 z-30 flex gap-[12px] p-[20px] bg-white'>
        <BasicButton type='button' size='full' variant='line' onClick={onPrev}>
          이전
        </BasicButton>
        <BasicButton type='button' size='full' onClick={handleSubmit(onNext)}>
          다음
        </BasicButton>
      </div>
    </>
  );
}
