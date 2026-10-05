import React from 'react';
import { Controller, useFormContext } from 'react-hook-form';
// 컴포넌트
import Input from '@/components/common/Input';
import BasicButton from '@/components/button/BasicButton';

export default function SignupStep2({ onPrev }) {
  const { control, watch, formState: { isSubmitting } } = useFormContext();
  const targetWeightValue = watch('targetWeight');

  // 목표 설정
  const goalsOption = [
    { value: '', label: '목표 선택' },
    { value: 'loss', label: '체중 감량' },
    { value: 'gain', label: '체중 증가' },
    { value: 'maintain', label: '건강증진 및 유지' },
  ];

  // 활동량
  const activityLevel = [
    { value: 'none', title: '운동 안함', sub: '일주일에 운동 0회' },
    { value: 'light', title: '가벼운 운동', sub: '주 1-3회, 30분씩 (산책, 요가)' },
    { value: 'moderate', title: '적당한 운동', sub: '주 3-5회, 45분씩 (헬스, 수영)' },
    { value: 'intense', title: '격렬한 운동', sub: '주 6-7회, 1시간씩 (크로스핏, 마라톤 훈련)' },
  ];

  return (
    <>
      <div className=' flex flex-col gap-[28px] min-h-[calc(100vh-220px)] px-[20px]'>
        {/* 목표 설정 */}
        <div className='flex flex-col gap-[8px]'>
          <label htmlFor='goals' className='text-sm font-bold text-gray-500'>
            목표 설정 <strong className='text-secondary font-extrabold'>*</strong>
          </label>
          <Controller
            name='goals'
            control={control}
            rules={{ required: '목표를 선택해주세요' }}
            render={({ field, fieldState }) => (
              <>
                <select
                  {...field}
                  id='goals'
                  className='w-full h-[48px] px-4 border border-[var(--color-gray-300)] rounded-lg transition-colors outline-none'
                >
                  {goalsOption.map((goal) => (
                    <option key={goal.value} value={goal.value}>
                      {goal.label}
                    </option>
                  ))}
                </select>
                {fieldState.error && (
                  <p className='text-[var(--color-error)] text-sm'>{fieldState.error.message}</p>
                )}
              </>
            )}
          />
        </div>

        {/* 목표 체중 */}
        <Controller
          name='targetWeight'
          control={control}
          rules={{
            validate: (value) => {
              if (!value) return true;
              if (!/^\d{1,3}(\.\d)?$/.test(value)) return '소수점 첫째 자리까지만 입력 가능합니다';
              if (value < 20) return '20kg 이상 입력해주세요.';
              if (value > 300) return '300kg 이하로 입력해주세요.';
            },
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='targetWeight' className='text-sm font-bold text-gray-500'>
                목표 체중
              </label>
              <Input
                {...field}
                id='targetWeight'
                type='number'
                noSpinner
                step='0.1'
                placeholder='00.0'
                endAdornment='kg'
                status={fieldState.error ? 'error' : 'default'}
                errorMessage={fieldState.error?.message}
                maxLength='3'
              />
              {!targetWeightValue && !fieldState.error && (
                <p className='text-[var(--color-error)] text-sm'>
                  감량(혹은 증량) 목표 달성을 위해 목표 체중을 입력해주세요
                </p>
              )}
            </div>
          )}
        />

        {/* 활동량*/}
        <Controller
          name='targetExercise'
          control={control}
          rules={{
            required: '활동량을 선택해주세요',
          }}
          render={({ field, fieldState }) => (
            <div className='flex flex-col gap-[8px]'>
              <label htmlFor='targetExercise' className='text-sm font-bold text-gray-500'>
                활동량 <strong className='text-secondary font-extrabold'>*</strong>
              </label>
              <div className='flex flex-col gap-[16px]'>
                {activityLevel.map((a) => (
                  <button
                    key={a.value}
                    type='button'
                    onClick={() => field.onChange(a.value)}
                    className={`flex flex-col gap-[8px] py-[20px] px-[28px] border border-gray-300 rounded-2xl text-left
                                ${field.value === a.value ? 'border-primary bg-primary-light' : ''}
                              `}
                  >
                    <h4
                      className={`font-bold text-lg ${field.value === a.value ? 'text-primary-dark' : ''}`}
                    >
                      {a.title}
                    </h4>
                    <p className='text-gray-500 text-sm'>{a.sub}</p>
                  </button>
                ))}
              </div>
              {fieldState.error && (
                <p className='text-[var(--color-error)] text-sm'>{fieldState.error.message}</p>
              )}
            </div>
          )}
        />
      </div>

      <div className='sticky bottom-0 z-30 flex gap-[12px] p-[20px] bg-white'>
        <BasicButton type='button' size='full' variant='line' onClick={onPrev} disabled={isSubmitting}>
          이전
        </BasicButton>
        <BasicButton size='full' type='submit' disabled={isSubmitting}>
          {isSubmitting ? '가입 중...' : '회원가입 완료'}
        </BasicButton>
      </div>
    </>
  );
}
